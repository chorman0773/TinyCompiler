package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.ast.BinaryOp;
import github.chorman0773.tiny.ast.ExpressionBinary;
import github.chorman0773.tiny.sema.MethodSignature;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.sema.ssa.expr.*;
import static github.chorman0773.tiny.ast.Type.*;

import org.objectweb.asm.*;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


public class JavaCodegen implements Codegen {
    private final ClassWriter writer;
    private final int ver;
    private final String outputName;
    private List<github.chorman0773.tiny.ast.Type> locals;

    private static int parseJavaVersion(String version){
        int val;
        if(version.startsWith("1."))
            val = Integer.parseInt(version.substring(2));
        else
            val = Integer.parseInt(version);
        if(val<7)
            throw new UnsupportedOperationException("Cannot compiled TINY to a jvm version less than 1.7 (attempted target "+version+")");
        return 44+val;
    }

    public JavaCodegen(String targetVersion,String outputName){
        ver = parseJavaVersion(targetVersion);
        writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        this.outputName = outputName;
    }

    private github.chorman0773.tiny.ast.Type getExprType(SSAExpression expr){
        if(expr instanceof ExprRead read){
            return read.getType();
        }else if(expr instanceof ExprCast cast){
            return cast.getType();
        }else if(expr instanceof ExprOp op){
            return getExprType(op.getLeft());
        }else if(expr instanceof ExprFloat)
            return github.chorman0773.tiny.ast.Type.Real;
        else if(expr instanceof ExprInt)
            return github.chorman0773.tiny.ast.Type.Int;
        else if(expr instanceof ExprLocal loc)
            return locals.get(loc.getLocalNumber());
        else
            throw new RuntimeException("ICE: Unsupported expression in codegen "+expr);
    }

    public void writeExpression(SSAExpression expr, MethodVisitor curr){
        if(expr instanceof ExprRead read){
            // Future versions may inline path initialization into the bootstrap method.
            // This will not break the existing ABI
            curr.visitLdcInsn(read.getPath());
            String desc = "(Ljava/lang/String;)"+toJavaType(read.getType()).getDescriptor();
            curr.visitInvokeDynamicInsn("read",desc,READ_BOOTSTRAP);
        }else if(expr instanceof ExprCast cast){
            var innerTy = getExprType(cast.getExpr());
            var outerTy = cast.getType();
            writeExpression(cast.getExpr(),curr);
            if(innerTy==outerTy)
                ; // no-op cast
            else if(innerTy==Int&&outerTy==Real)
                curr.visitInsn(Opcodes.I2D);
            else if(innerTy==Real&&outerTy==Int)
                curr.visitInsn(Opcodes.D2I);
            else
                curr.visitInvokeDynamicInsn("cast",Type.getMethodDescriptor(toJavaType(outerTy),toJavaType(innerTy)),CAST_BOOTSTRAP);
        }else if(expr instanceof ExprInt i){
            int value = i.getValue();

            if(-1<=value&&value<=5)
                curr.visitInsn(Opcodes.ICONST_0+value);
            else
                curr.visitLdcInsn(value);
        }else if(expr instanceof ExprFloat f){
            double value = f.getValue();
            if(value==0.0)
                curr.visitInsn(Opcodes.DCONST_0);
            else if(value==1.0)
                curr.visitInsn(Opcodes.DCONST_1);
            else
                curr.visitLdcInsn(value);
        }else if(expr instanceof ExprOp op){
            var ty = getExprType(op);
            writeExpression(op.getLeft(),curr);
            writeExpression(op.getRight(),curr);
            Type realTy = toJavaType(ty);
            if(ty!=String){
                int opcode = switch(op.getBinaryOp()){
                    case Add -> Opcodes.IADD;
                    case Sub -> Opcodes.ISUB;
                    case Div -> Opcodes.IDIV;
                    case Mul -> Opcodes.IMUL;
                };
                curr.visitInsn(realTy.getOpcode(opcode));
            }else {
                String name = op.getBinaryOp().toString();
                curr.visitInvokeDynamicInsn(name, Type.getMethodDescriptor(realTy, realTy, realTy), BINOP_BOOTSTRAP);
            }
        }else if(expr instanceof ExprCall call){
            for(var arg : call.getParameters())
                writeExpression(arg,curr);
            MethodSignature sig = call.getSignature();
            Type retTy = toJavaType(sig.ret());

            Type[] paramTys = sig.params().stream().map(this::toJavaType).toArray(Type[]::new);
            curr.visitMethodInsn(Opcodes.INVOKESTATIC,this.outputName, call.getFunction(), Type.getMethodDescriptor(retTy,paramTys),false);
        }
    }

    public void writeMethod(SSAMethodDeclaration decl){

    }

    public Type toJavaType(github.chorman0773.tiny.ast.Type ty){
        return switch(ty){
            case Int -> Type.INT_TYPE;
            case Real -> Type.DOUBLE_TYPE;
            case String -> Type.getObjectType("java/lang/String");
        };
    }

    private static final String BOOTSTRAP_SIGNATURE ="(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";


    private static final Handle READ_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/IOBootstraps","read",BOOTSTRAP_SIGNATURE,false);
    private static final Handle WRITE_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/IOBootstraps","write",BOOTSTRAP_SIGNATURE,false);
    private static final Handle EXIT_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, "github/chorman0773/tiny/stdlib/ProcBootstraps","exit",BOOTSTRAP_SIGNATURE,false);
    private static final Handle MAIN_BOOSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/ProcBootstraps","main",BOOTSTRAP_SIGNATURE, false);
    private static final Handle CAST_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, "github/chorman0773/tiny/stdlib/ProcBootstraps","cast",BOOTSTRAP_SIGNATURE,false);
    private static final Handle BINOP_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/ProcBootstraps","binaryOperator",BOOTSTRAP_SIGNATURE,false);


    @Override
    public void writeIR(SSAProgram prg) {
        writer.visit(ver,Opcodes.ACC_PUBLIC|Opcodes.ACC_SUPER|Opcodes.ACC_FINAL,outputName,null,"github/chorman0773/tiny/stdlib/TinyFile",null);

        Handle mainTarget = null;

        for(var decl : prg.getDeclarations()){
            writeMethod(decl);

            if(decl.isMain()){
                Type retTy = toJavaType(decl.getReturnType());

                Type[] paramTys = decl.getParameters().stream().map(this::toJavaType).toArray(Type[]::new);

                Type methodTy = Type.getMethodType(retTy,paramTys);
                mainTarget = new Handle(Opcodes.H_INVOKESTATIC,outputName,decl.getName(),methodTy.getDescriptor(),false);
            }
        }

        if(mainTarget!=null){
            MethodVisitor visit = writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
            visit.visitInvokeDynamicInsn(mainTarget.getName(), mainTarget.getDesc(), MAIN_BOOSTRAP);
            visit.visitInvokeDynamicInsn("exit","(I)V",EXIT_BOOTSTRAP);
            visit.visitInsn(Opcodes.RETURN);
        }

    }

    @Override
    public void writeOutput(OutputStream stream) throws IOException {

    }
}
