package github.chorman0773.tiny.sema.ssa;

import com.sun.jdi.Method;
import github.chorman0773.tiny.ast.*;
import github.chorman0773.tiny.sema.ConversionError;
import github.chorman0773.tiny.sema.MethodSignature;
import github.chorman0773.tiny.sema.ssa.expr.*;
import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;
import github.chorman0773.tiny.sema.ssa.stat.*;


import java.util.*;
import java.util.stream.Collectors;

public class SSAConverter {

    private Map<String, MethodSignature> signature;

    private List<BasicBlock> basicBlocks;

    static class BasicBlockBuilder{
        BasicBlockBuilder(int num){
            this.num = num;
            this.localNames = new HashMap<>();
            this.localNums = 0;
            this.locals = new ArrayList<>();
            this.stats = new ArrayList<>();
            this.next = -1;
        }
        int num;
        Map<String,Integer> localNames;
        int localNums;
        List<Type> locals;
        List<SSAStatement> stats;
        int next;

        BasicBlock build(){
            return new BasicBlock(num,next,locals,stats);
        }
    }

    private int nextBlock;
    private List<String> localNames;
    private Map<String, Type> localTypes;
    private BasicBlockBuilder currBB;

    public SSAConverter(){
        this.signature = new HashMap<>();
        this.reset();
    }

    public void reset(){
        this.basicBlocks = new ArrayList<>();

        this.nextBlock = 1;
        this.localNames = new ArrayList<>();
        this.localTypes = new HashMap<>();
        this.currBB = new BasicBlockBuilder(0);
    }

    public Type typecheckExpr(SSAExpression expr){
        if(expr instanceof ExprLocal local){
            return this.currBB.locals.get(local.getLocalNumber());
        }else if(expr instanceof ExprOp op){
            Type leftTy = typecheckExpr(op.getLeft());
            if(leftTy!=typecheckExpr(op.getRight()))
                throw new ConversionError("Cannot apply binary op "+op+" left type ("+leftTy+") is not "+typecheckExpr(op.getRight()));
            return leftTy;
        }else if(expr instanceof ExprInt){
            return Type.Int;
        }else if(expr instanceof ExprFloat){
            return Type.Real;
        }else if(expr instanceof ExprCast cast){
            return cast.getType();
        }else if(expr instanceof ExprCall call){
            return signature.get(call.getFunction()).ret();
        }else
            throw new ConversionError("Unrecognized expression type "+expr);
    }


    public SSAExpression convertExpr(github.chorman0773.tiny.ast.Expression expr){
        if(expr instanceof ExpressionId id){
            Integer local = currBB.localNames.get(id.getIdentifier());
            if(local==null)
                throw new ConversionError("Attempt to use undefined or uninitialized local "+id.getIdentifier());
            int localN = local;
            return new ExprLocal(localN);
        }else if(expr instanceof ExpressionBinary bin){
            SSAExpression left = convertExpr(bin.getLeft());
            SSAExpression right = convertExpr(bin.getRight());
            Type leftTy = typecheckExpr(left);
            Type rightTy = typecheckExpr(right);

            if(leftTy==rightTy)
                return new ExprOp(bin.getOperator(),left,right);
            else if(leftTy==Type.Int)
                return new ExprOp(bin.getOperator(), new ExprCast(rightTy,left),right);
            else
                return new ExprOp(bin.getOperator(), left, new ExprCast(leftTy, right));
        }else if(expr instanceof ExpressionCall call){
            List<SSAExpression> args =call.getParameters().stream().map(this::convertExpr).collect(Collectors.toList());
            MethodSignature sig = signature.get(call.getMethodName());
            if(sig==null)
                throw new ConversionError("Attempt to call undeclared function "+call.getMethodName());
            if(sig.params().size()!=args.size())
                throw new ConversionError("Attempt to call function "+call.getMethodName()+" ("+sig.params().size()+" parameters) with "+args.size()+" arguments.");
            for(int i = 0;i<sig.params().size();i++){
                var arg = args.get(i);
                if(typecheckExpr(arg)!=sig.params().get(i))
                    args.set(i,new ExprCast(sig.params().get(i),arg));
            }
            return new ExprCall(call.getMethodName(),args,sig);
        }else if(expr instanceof ParenExpr paren){
            return convertExpr(paren.getInner());
        }else if(expr instanceof ExpressionNumber num){
            double value = num.getValue();
            if(value == ((int)value) && !Double.isInfinite(value)){
                int intVal = (int)value;
                return new ExprInt(intVal);
            }else{
                return new ExprFloat(value);
            }
        }else
            throw new ConversionError("Unrecognized Expression " + expr);
    }

    public void convertStatement(github.chorman0773.tiny.ast.Statement stat){
        if(stat instanceof StatementAssignment assign){
            String id = assign.getIdent();
            SSAExpression expr = convertExpr(assign.getValue());

            Type ty = localTypes.get(id);
            if(ty==null)
                throw new ConversionError("Attempt to assign to undeclared local variable "+id);

            Type assignTy = typecheckExpr(expr);

            if(assignTy!=ty){
                expr = new ExprCast(ty,expr);
            }

            int newLoc = currBB.localNums++;

            currBB.locals.add(ty);

            currBB.stats.add(new StatDeclaration(ty,newLoc,expr));
            if(currBB.localNames.containsKey(id)){
                currBB.stats.add(new StatStoreDead(currBB.localNames.get(id)));
            }
            currBB.localNames.put(id,newLoc);
        }else if(stat instanceof StatementDeclaration decl){
            Type ty = decl.getType();
            String name = decl.getName();

            if(localTypes.putIfAbsent(name,ty)!=null)
                throw new ConversionError("Attempt to refine existing local variable "+name);
            localNames.add(name);
            var expr = decl.getInitializer();
            if(expr.isPresent()){
                currBB.locals.add(ty);

                SSAExpression init = convertExpr(expr.get());
                Type assignTy = typecheckExpr(init);

                if(assignTy!=ty){
                    init = new ExprCast(ty,init);
                }

                currBB.stats.add(new StatDeclaration(ty,currBB.localNums++,init));
            }
        }else if(stat instanceof StatementRead read){
            String id = read.getIdent();
            Type ty = localTypes.get(id);
            if(ty==null)
                throw new ConversionError("Attempt to assign to undeclared local variable "+id);
            int newLoc = currBB.localNums++;
            currBB.stats.add(new StatDeclaration(ty,newLoc,new ExprRead(ty,read.getPath())));
            if(currBB.localNames.containsKey(id)){
                currBB.stats.add(new StatStoreDead(currBB.localNames.get(id)));
            }
            currBB.localNames.put(id,newLoc);
        }else if(stat instanceof StatementWrite write){
            SSAExpression expr = convertExpr(write.getValue());
            currBB.stats.add(new StatWrite(expr,write.getPath()));
        }else if(stat instanceof StatementReturn ret){
            currBB.stats.add(new StatReturn(convertExpr(ret.getValue())));
        }else if(stat instanceof StatementBlock block){
            for (Statement s : block.getBlock().getStatements()){
                convertStatement(s);
            }
        }else if(stat instanceof StatementIf cond){
            BooleanExpr ctrl = cond.getControl();
            Statement then = cond.getIf();
            Optional<Statement> orelse = cond.getElse();
            BasicBlockBuilder thenBB = new BasicBlockBuilder(nextBlock++);
            BasicBlockBuilder lastBB = currBB;
            Map<Integer,Integer> localMap = new HashMap<>();
            for(String name : this.localNames){
                if(!lastBB.localNames.containsKey(name))
                    continue;
                int localName = thenBB.localNums++;
                thenBB.locals.add(this.localTypes.get(name));
                thenBB.locals.add(this.localTypes.get(name));
                localMap.put(currBB.localNames.get(name),localName);
                thenBB.localNames.put(name,localName);
            }
            lastBB.stats.add(new StatBranchCompare(thenBB.num,ctrl.getOperator(),convertExpr(ctrl.getLeft()),convertExpr(ctrl.getRight()),localMap));
            currBB = thenBB;
            convertStatement(then);

            if(orelse.isPresent()){
                BasicBlockBuilder elseBB = new BasicBlockBuilder(nextBlock++);
                lastBB.next = elseBB.num;
                localMap = new HashMap<>();
                for(String name : this.localNames){
                    if(!lastBB.localNames.containsKey(name))
                        continue;
                    int localName = elseBB.localNums++;
                    elseBB.locals.add(this.localTypes.get(name));
                    localMap.put(lastBB.localNames.get(name),localName);
                    elseBB.localNames.put(name,localName);
                }
                currBB = elseBB;
                lastBB.stats.add(new StatBranch(elseBB.num,localMap));
                this.basicBlocks.add(lastBB.build());
                lastBB = elseBB;
                convertStatement(orelse.get());
            }

            BasicBlockBuilder nextBB = new BasicBlockBuilder(nextBlock++);
            Map<Integer,Integer> thenLocalMap = new HashMap<>();
            localMap = new HashMap<>();
            for(String name : this.localNames){
                if(!thenBB.localNames.containsKey(name)&&lastBB.localNames.containsKey(name))
                    continue;
                int localName = nextBB.localNums++;
                nextBB.locals.add(this.localTypes.get(name));
                localMap.put(lastBB.localNames.get(name),localName);
                thenLocalMap.put(thenBB.localNames.get(name),localName);
                nextBB.localNames.put(name,localName);
            }
            thenBB.stats.add(new StatBranch(nextBB.num,thenLocalMap));
            lastBB.stats.add(new StatBranch(nextBB.num, localMap));
            this.basicBlocks.add(lastBB.build());
            this.basicBlocks.add(thenBB.build());
            currBB = nextBB;
        }else
            throw new ConversionError("Unrecognized statement "+ stat);
    }

    public SSAMethodDeclaration convertMethod(github.chorman0773.tiny.ast.MethodDeclaration method){
        List<Type> params = new ArrayList<>();
        for(var param : method.getParameters()){
            this.localNames.add(param.getName());
            this.localTypes.put(param.getName(),param.getType());
            int localName = currBB.localNums++;
            currBB.locals.add(param.getType());
            currBB.localNames.put(param.getName(),localName);
            params.add(param.getType());
        }

        for(var stat : method.getBlock().getStatements())
            convertStatement(stat);
        if(!currBB.stats.get(currBB.stats.size()-1).isTerminator()){
            if(method.isMain())
                currBB.stats.add(new StatReturn(new ExprInt(0)));
            else
                throw new ConversionError("No return statement from function "+method.getName());
        }
        this.basicBlocks.add(currBB.build());

        return new SSAMethodDeclaration(params,method.isMain(),method.getName(),method.returnType(),this.basicBlocks);
    }

    public SSAProgram convertProgram(github.chorman0773.tiny.ast.Program prg){
        for(var decl : prg.getDeclarations()){
            if(signature.putIfAbsent(decl.getName(),new MethodSignature(decl.returnType(), decl.getParameters().stream().map(Parameter::getType).toList()))!=null)
                throw new ConversionError("Redefinition of method "+decl.getName());
        }
        List<SSAMethodDeclaration> decls = new ArrayList<>();
        for(var decl : prg.getDeclarations()){
            decls.add(convertMethod(decl));
            this.reset();
        }
        return new SSAProgram(decls);
    }
}
