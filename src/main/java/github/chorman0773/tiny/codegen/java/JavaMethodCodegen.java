package github.chorman0773.tiny.codegen.java;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.BasicBlock;
import github.chorman0773.tiny.sema.ssa.MethodSignature;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.expr.*;
import github.chorman0773.tiny.sema.ssa.stat.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class JavaMethodCodegen {
    private final MethodVisitor visit;
    private Map<Integer,Label> bbLabels;
    private Map<Integer, Label> localEndLabels;
    private Set<Integer> localsLive;
    private BasicBlock currBB;
    private List<BasicBlock> bbs;
    private String clName;
    private Map<Integer,Integer> localToReal;
    private int nextRealLocal;
    private int stackDepth;
    private int maxStackDepth;

    public JavaMethodCodegen(MethodVisitor visit,String clName){
        this.clName = clName;
        this.visit = visit;
        this.bbLabels = new HashMap<>();
        this.localEndLabels = new HashMap<>();
        this.localsLive = new HashSet<>();
        this.localToReal = new HashMap<>();
    }

    private int tinyLocalToReal(int local){
        return localToReal.get(local);
    }

    public void writeMethod(SSAMethodDeclaration methodDecl){
        bbs = Collections.unmodifiableList(methodDecl.getBlocks());
        int paramNo = 0;
        Label start = new Label();
        visit.visitLabel(start);
        for(var ty : methodDecl.getParameters()){
            int realLocal = nextRealLocal++;
            int localNo = paramNo++;
            localToReal.put(localNo,realLocal);
            Label end = new Label();
            localEndLabels.put(realLocal,end);
            // visit.visitLocalVariable("_"+localNo,JavaCodegen.toJavaType(ty).getDescriptor(),null,start,end,realLocal);
            localsLive.add(realLocal);
            if(ty==Type.Real) // double uses 2 stack slots in JVM, so we need to bump nextRealLocal by one
                nextRealLocal++;
        }
        for(BasicBlock bb : bbs){
            currBB = bb;
            if(bb.getNum()!=0)
                visit.visitFrame(Opcodes.F_SAME,nextRealLocal,null,0,null);
            bbLabels.putIfAbsent(bb.getNum(),new Label());
            Label l = bbLabels.get(bb.getNum());
            visit.visitLabel(l);
            for(SSAStatement stat : bb.getStats()){
                writeStatement(stat);
            }
        }
        System.err.printf("Finished Codegening function with %d stack depth and %d locals",maxStackDepth,this.nextRealLocal);
        visit.visitMaxs(maxStackDepth,this.nextRealLocal);
    }

    private Type getExprType(SSAExpression expr){
        if(expr instanceof ExprInt){
            return Type.Int;
        }else if(expr instanceof ExprFloat){
            return Type.Real;
        }else if(expr instanceof ExprCast cast){
            return cast.getType();
        }else if(expr instanceof ExprOp op){
            return getExprType(op.getLeft());
        }else if(expr instanceof ExprCall call){
            return call.getSignature().ret();
        }else if(expr instanceof ExprRead read){
            return read.getType();
        }else if(expr instanceof ExprLocal loc){
            return currBB.getLocals().get(loc.getLocalNumber());
        }else
            throw new RuntimeException("ICE: Invalid SSA Expression" + expr);
    }

    private void writeExpr(SSAExpression expr){
        if(expr instanceof ExprInt i){
            stackDepth++;
            maxStackDepth = Integer.max(stackDepth,maxStackDepth);
            int val = i.getValue();
            if(val>=-1&&val<=5)
                visit.visitInsn(Opcodes.ICONST_0+val);
            else if(val>=-128&&val<128)
                visit.visitIntInsn(Opcodes.BIPUSH,val);
            else if(val>=-0x8000&&val<0x8000)
                visit.visitIntInsn(Opcodes.SIPUSH,val);
            else
                visit.visitLdcInsn(val);
        }else if(expr instanceof ExprFloat f){
            stackDepth+=2;
            maxStackDepth = Integer.max(stackDepth,maxStackDepth);
            double val = f.getValue();
            if(val==0.0)
                visit.visitInsn(Opcodes.DCONST_0);
            else if(val==1.0)
                visit.visitInsn(Opcodes.DCONST_1);
            else
                visit.visitLdcInsn(val);
        }else if(expr instanceof ExprCast cast){
            SSAExpression inner = cast.getExpr();
            var innerTy = getExprType(inner);
            var outerTy = cast.getType();
            if(outerTy!=innerTy&&outerTy==Type.Real) {
                stackDepth++;
                maxStackDepth = Integer.max(stackDepth,maxStackDepth);
            }else if(outerTy!=innerTy&&innerTy==Type.Real){
                stackDepth--;
            }
            this.writeExpr(inner);
            if(innerTy==Type.Real&&outerTy==Type.Int)
                visit.visitInsn(Opcodes.D2I);
            else if(innerTy==Type.Int&&outerTy==Type.Real)
                visit.visitInsn(Opcodes.I2D);
            else if(innerTy!=outerTy){
                var desc = JavaCodegen.toJavaSignature(new MethodSignature(outerTy,List.of(innerTy)));
                visit.visitInvokeDynamicInsn("cast",desc, JavaCodegen.CAST_BOOTSTRAP);
            }
        }else if(expr instanceof ExprOp op){
            SSAExpression left = op.getLeft();
            SSAExpression right = op.getRight();
            var ty = getExprType(left);
            this.writeExpr(left);
            this.writeExpr(right);
            if(ty==Type.Int){
                stackDepth--;
                switch(op.getBinaryOp()){
                    case Add -> visit.visitInsn(Opcodes.IADD);
                    case Sub -> visit.visitInsn(Opcodes.ISUB);
                    case Mul -> visit.visitInsn(Opcodes.IMUL);
                    case Div -> visit.visitInsn(Opcodes.IDIV);
                }
            }else if(ty==Type.Real){
                stackDepth -= 2;
                switch(op.getBinaryOp()){
                    case Add -> visit.visitInsn(Opcodes.DADD);
                    case Sub -> visit.visitInsn(Opcodes.DSUB);
                    case Mul -> visit.visitInsn(Opcodes.DMUL);
                    case Div -> visit.visitInsn(Opcodes.DDIV);
                }
            }else{
                stackDepth--;
                String name = switch(op.getBinaryOp()){
                    case Add -> "add";
                    case Sub -> "sub";
                    case Mul -> "mul";
                    case Div -> "div";
                };

                String signature = JavaCodegen.toJavaSignature(new MethodSignature(ty,List.of(ty,ty)));
                visit.visitInvokeDynamicInsn(name,signature,JavaCodegen.BINOP_BOOTSTRAP);
            }
        }else if(expr instanceof ExprRead read){
            var ty = read.getType();
            if(ty==Type.Real)
                stackDepth += 2;
            else
                stackDepth++;
            maxStackDepth = Integer.max(stackDepth,maxStackDepth);
            visit.visitLdcInsn(read.getPath());
            String signature = "(Ljava/lang/String;)"+JavaCodegen.toJavaType(ty).getDescriptor();
            visit.visitInvokeDynamicInsn("read",signature,JavaCodegen.READ_BOOTSTRAP);
        }else if(expr instanceof ExprCall call){
            String signature = JavaCodegen.toJavaSignature(call.getSignature());
            for(var arg : call.getParameters())
                this.writeExpr(arg);
            stackDepth -= call.getParameters().size();
            visit.visitMethodInsn(Opcodes.INVOKESTATIC,this.clName,call.getFunction().name(),signature,false);
        }else if(expr instanceof ExprLocal n){
            System.err.println("Codegening Local:" + n);
            int loc = n.getLocalNumber();
            int realLoc = this.tinyLocalToReal(loc);
            Type ty = currBB.getLocals().get(loc);
            if(ty==Type.Real)
                stackDepth += 2;
            else
                stackDepth++;
            maxStackDepth = Integer.max(stackDepth,maxStackDepth);

            switch(ty){
                case Int -> visit.visitVarInsn(Opcodes.ILOAD,realLoc);
                case Real -> visit.visitVarInsn(Opcodes.DLOAD,realLoc);
                case String -> visit.visitVarInsn(Opcodes.ALOAD,realLoc);
            }
        }else
            throw new RuntimeException("Internal Error: Unexpected Expression" + expr);
    }

    private void doRemap(Map<Integer,Integer> remaps){
        for(var locals : remaps.entrySet()){
            Type locTy = currBB.getLocals().get(locals.getKey());
            int realIncomingLocal = tinyLocalToReal(locals.getKey());
            int realTargetLocal;
            if(!localToReal.containsKey(locals.getValue())){
                realTargetLocal = nextRealLocal++;
                localToReal.put(locals.getValue(),realTargetLocal);
                if(locTy==Type.Real)
                    nextRealLocal++;
            }else{
                realTargetLocal = tinyLocalToReal(locals.getValue());
            }
            if(locTy==Type.Real)
                stackDepth += 2;
            else
                stackDepth++;
            maxStackDepth = Integer.max(stackDepth,maxStackDepth);
            switch(locTy){
                case Int -> visit.visitVarInsn(Opcodes.ILOAD,realIncomingLocal);
                case Real -> visit.visitVarInsn(Opcodes.DLOAD, realIncomingLocal);
                case String -> visit.visitVarInsn(Opcodes.ALOAD, realIncomingLocal);
            }
            if(!this.localEndLabels.containsKey(locals.getValue())){
                Label start = new Label();
                visit.visitLabel(start);
                Label end = new Label();
                this.localEndLabels.put(realTargetLocal,end);
                // visit.visitLocalVariable("_"+locals.getValue(),JavaCodegen.toJavaType(locTy).getDescriptor(),null,start,end,realTargetLocal);
            }
            if(locTy==Type.Real)
                stackDepth -= 2;
            else
                stackDepth--;
            switch(locTy){
                case Int -> visit.visitVarInsn(Opcodes.ISTORE,realTargetLocal);
                case Real -> visit.visitVarInsn(Opcodes.DSTORE, realTargetLocal);
                case String -> visit.visitVarInsn(Opcodes.ASTORE, realTargetLocal);
            }
        }
    }

    private void writeStatement(SSAStatement stat){
        stackDepth = 0;
        if(stat instanceof StatDeclaration decl){
            System.err.println("Codegening Declaration: "+decl);
            Label begin = new Label();
            Label end = new Label();

            writeExpr(decl.getInitializer());
            visit.visitLabel(begin);
            int loc = decl.getLocalNum();
            int realLoc = nextRealLocal++;
            this.localToReal.put(loc,realLoc);
            switch(decl.getType()){
                case Int -> visit.visitVarInsn(Opcodes.ISTORE,realLoc);
                case Real -> visit.visitVarInsn(Opcodes.DSTORE, realLoc);
                case String -> visit.visitVarInsn(Opcodes.ASTORE, realLoc);
            }
            if(decl.getType()==Type.Real)
                nextRealLocal++;
            // visit.visitLocalVariable("_"+loc, JavaCodegen.toJavaType(decl.getType()).getDescriptor(),null,begin,end,realLoc);
            this.localsLive.add(decl.getLocalNum());
            this.localEndLabels.put(decl.getLocalNum(),end);
        }else if(stat instanceof StatWrite write){
            visit.visitLdcInsn(write.getPath());
            writeExpr(write.getExpr());
            String signature = "(Ljava/lang/String;"+JavaCodegen.toJavaType(this.getExprType(write.getExpr())).getDescriptor()+")V";
            visit.visitInvokeDynamicInsn("write",signature,JavaCodegen.WRITE_BOOTSTRAP);
        }else if(stat instanceof StatStoreDead storeDead){
            Label end = this.localEndLabels.get(tinyLocalToReal(storeDead.getLocalNumber()));
            visit.visitLabel(end);
        }else if(stat instanceof StatBranchCompare branch){
            var left = branch.getLeft();
            var right = branch.getRight();
            this.writeExpr(left);
            this.writeExpr(right);
            Type ty = this.getExprType(left);
            this.doRemap(branch.getRemaps());
            Label targ = bbLabels.computeIfAbsent(branch.getTargetNumber(),i->new Label());
            switch(ty){
                case Int -> {
                    switch(branch.getOp()){
                        case CmpEq -> visit.visitJumpInsn(Opcodes.IF_ICMPEQ,targ);
                        case CmpNe -> visit.visitJumpInsn(Opcodes.IF_ICMPNE,targ);
                    }
                }
                case Real -> {
                    visit.visitInsn(Opcodes.DCMPL);
                    switch(branch.getOp()){
                        case CmpEq -> visit.visitJumpInsn(Opcodes.IFEQ,targ);
                        case CmpNe -> visit.visitJumpInsn(Opcodes.IFNE,targ);
                    }
                }
                default -> {
                    String desc = JavaCodegen.toJavaType(ty).getDescriptor();
                    String sig = "("+desc+desc+")I";
                    visit.visitInvokeDynamicInsn("compare",desc,JavaCodegen.CMP_BOOTSTRAP);
                    switch(branch.getOp()){
                        case CmpEq -> visit.visitJumpInsn(Opcodes.IFEQ,targ);
                        case CmpNe -> visit.visitJumpInsn(Opcodes.IFNE,targ);
                    }
                }
            }
        }else if(stat instanceof StatBranch branch){
            this.doRemap(branch.getRemaps());
            Label targ = bbLabels.computeIfAbsent(branch.getTargetNumber(),i->new Label());
            for(int i : currBB.getLocals().keySet()){
                if(this.localsLive.contains(i)) {
                    this.localsLive.remove(i);
                    Label l = localEndLabels.get(i);
                    visit.visitLabel(l);
                }
            }
            visit.visitJumpInsn(Opcodes.GOTO,targ);
        }else if(stat instanceof StatReturn ret){
            var expr = ret.getExpr();
            Type ty = this.getExprType(expr);
            this.writeExpr(expr);
            for(int i : currBB.getLocals().keySet()){
                if(this.localsLive.contains(i)) {
                    this.localsLive.remove(i);
                    Label l = localEndLabels.get(i);
                    visit.visitLabel(l);
                }
            }
            switch(ty){
                case Int -> visit.visitInsn(Opcodes.IRETURN);
                case Real -> visit.visitInsn(Opcodes.DRETURN);
                case String -> visit.visitInsn(Opcodes.ARETURN);
            }
        }else if(stat instanceof StatNop){
            visit.visitInsn(Opcodes.NOP);
        }else if(stat instanceof StatUnreachable){
            visit.visitInvokeDynamicInsn("unreachable","()Ljava/lang/Throwable;",JavaCodegen.UNREACHBALE_BOOTSTRAP);
            visit.visitInsn(Opcodes.ATHROW);
        }else
            throw new RuntimeException("Internal Error: Unexpected statement " + stat);
    }
}
