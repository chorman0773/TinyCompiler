package github.chorman0773.tiny.sema.ssa;

import github.chorman0773.tiny.ast.*;
import github.chorman0773.tiny.sema.ConversionError;
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
            this.locals = new HashMap<>();
            this.stats = new ArrayList<>();
            this.next = -1;
        }
        int num;
        Map<String,Integer> localNames;
        int localNums;
        Map<Integer,Type> locals;
        List<SSAStatement> stats;
        int next;

        BasicBlock build(){
            return new BasicBlock(num,next,locals,stats);
        }

        public Optional<SSAStatement> getLastStatement(){
            if(stats.size()==0)
                return Optional.empty();
            else
                return Optional.of(stats.get(stats.size()-1));
        }

        public boolean hasTerminator(){
            return stats.size()!=0&&stats.get(stats.size()-1).isTerminator();
        }
    }

    private int nextBlock;
    private int nextLocal;
    private Map<String, Type> localTypes;
    private BasicBlockBuilder currBB;
    private MethodSignature currSig;

    public SSAConverter(){
        this.signature = new HashMap<>();
        this.reset();
    }

    public void reset(){
        this.basicBlocks = new ArrayList<>();

        this.nextBlock = 1;
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
            return signature.get(call.getFunction().name()).ret();
        }else
            throw new ConversionError("Unrecognized expression type "+expr);
    }


    public SSAExpression convertExpr(github.chorman0773.tiny.ast.Expression expr){
        if(expr instanceof ExpressionId id){
            if(!localTypes.containsKey(id.getIdentifier().name()))
                throw new ConversionError("Attempt to use undefined local "+id.getIdentifier());
            Integer local = currBB.localNames.get(id.getIdentifier().name());
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
            MethodSignature sig = signature.get(call.getMethodName().name());
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
            String id = assign.getIdent().name();
            SSAExpression expr = convertExpr(assign.getValue());

            Type ty = localTypes.get(id);
            if(ty==null)
                throw new ConversionError("Attempt to assign to undeclared local variable "+id);

            Type assignTy = typecheckExpr(expr);

            if(assignTy!=ty){
                expr = new ExprCast(ty,expr);
            }

            int newLoc = nextLocal++;

            currBB.locals.put(newLoc,ty);

            currBB.stats.add(new StatDeclaration(ty,newLoc,expr));
            if(currBB.localNames.containsKey(id)){
                currBB.stats.add(new StatStoreDead(currBB.localNames.get(id)));
            }
            currBB.localNames.put(id,newLoc);
        }else if(stat instanceof StatementDeclaration decl){
            Type ty = decl.getType();
            String name = decl.getName().name();

            if(localTypes.putIfAbsent(name,ty)!=null)
                throw new ConversionError("Attempt to redefine existing local variable "+name);
            var expr = decl.getInitializer();
            if(expr.isPresent()){
                int newLoc = nextLocal++;
                currBB.locals.put(newLoc,ty);

                SSAExpression init = convertExpr(expr.get());
                Type assignTy = typecheckExpr(init);

                if(assignTy!=ty){
                    init = new ExprCast(ty,init);
                }

                currBB.stats.add(new StatDeclaration(ty,newLoc,init));
                currBB.localNames.put(name,newLoc);
            }
        }else if(stat instanceof StatementRead read){
            String id = read.getIdent();
            Type ty = localTypes.get(id);
            if(ty==null)
                throw new ConversionError("Attempt to assign to undeclared local variable "+id);
            int newLoc = nextLocal++;
            currBB.stats.add(new StatDeclaration(ty,newLoc,new ExprRead(ty,read.getPath())));
            if(currBB.localNames.containsKey(id)){
                currBB.stats.add(new StatStoreDead(currBB.localNames.get(id)));
            }
            currBB.locals.put(newLoc,ty);
            currBB.localNames.put(id,newLoc);
        }else if(stat instanceof StatementWrite write){
            SSAExpression expr = convertExpr(write.getValue());
            currBB.stats.add(new StatWrite(expr,write.getPath()));
        }else if(stat instanceof StatementReturn ret){
            SSAExpression expr = convertExpr(ret.getValue());
            Type ty = typecheckExpr(expr);
            if(ty!=currSig.ret())
                expr = new ExprCast(currSig.ret(),expr);
            currBB.stats.add(new StatReturn(expr));
        }else if(stat instanceof StatementBlock block){
            Map<String,Type> prevNames = new HashMap<>(localTypes);
            for (Statement s : block.getBlock().getStatements()){
                convertStatement(s);
            }
            for(String name : localTypes.keySet()){
                if(prevNames.containsKey(name)||!currBB.localNames.containsKey(name))
                    continue;
                int localName = currBB.localNames.get(name);
                currBB.stats.add(new StatStoreDead(localName));
            }
            localTypes = prevNames;
        }else if(stat instanceof StatementIf cond){
            BooleanExpr ctrl = cond.getControl();

            SSAExpression left = convertExpr(ctrl.getLeft());
            SSAExpression right = convertExpr(ctrl.getRight());

            Type leftTy = typecheckExpr(left);
            Type rightTy = typecheckExpr(right);


            if(leftTy!=rightTy){
                if(leftTy == Type.String)
                    right = new ExprCast(leftTy,right);
                else if(rightTy == Type.String)
                    left = new ExprCast(rightTy, left);
                else if(leftTy == Type.Real)
                    right = new ExprCast(leftTy,right);
                else
                    left = new ExprCast(rightTy, left);
            }

            Statement then = cond.getIf();
            Optional<Statement> orelse = cond.getElse();
            BasicBlockBuilder thenBB = new BasicBlockBuilder(nextBlock++);
            BasicBlockBuilder lastBB = currBB;
            Map<Integer,Integer> localMap = new HashMap<>();
            Map<String,Type> prevNames = new HashMap<>(localTypes);
            for(String name : this.localTypes.keySet()){
                if(!lastBB.localNames.containsKey(name))
                    continue;
                int localName = nextLocal++;
                thenBB.locals.put(localName,this.localTypes.get(name));
                localMap.put(currBB.localNames.get(name),localName);
                thenBB.localNames.put(name,localName);
            }
            lastBB.stats.add(new StatBranchCompare(thenBB.num,ctrl.getOperator(),left,right,localMap));
            lastBB.next = thenBB.num;
            currBB = thenBB;
            convertStatement(then);

            if(orelse.isPresent()) {
                this.localTypes = new HashMap<>(prevNames);
                BasicBlockBuilder elseBB = new BasicBlockBuilder(nextBlock++);
                currBB.next = elseBB.num;
                localMap = new HashMap<>();
                for (String name : this.localTypes.keySet()) {
                    if (!lastBB.localNames.containsKey(name))
                        continue;
                    int localName = nextLocal++;
                    elseBB.locals.put(localName, this.localTypes.get(name));
                    localMap.put(lastBB.localNames.get(name), localName);
                    elseBB.localNames.put(name, localName);
                }
                currBB = elseBB;
                lastBB.stats.add(new StatBranch(elseBB.num, localMap));
                this.basicBlocks.add(lastBB.build());
                lastBB = elseBB;
                convertStatement(orelse.get());
            }
            boolean thenHasTerm = thenBB.hasTerminator();
            boolean elseHasTerm = lastBB.hasTerminator();

            if(!(thenHasTerm&&elseHasTerm)){
                BasicBlockBuilder nextBB = new BasicBlockBuilder(nextBlock++);
                Map<Integer,Integer> thenLocalMap = new HashMap<>();
                localMap = new HashMap<>();
                for(String name : prevNames.keySet()){
                    if(!thenBB.localNames.containsKey(name)&&lastBB.localNames.containsKey(name))
                        continue;
                    int localName = nextLocal++;
                    nextBB.locals.put(localName,this.localTypes.get(name));
                    localMap.put(lastBB.localNames.get(name),localName);
                    thenLocalMap.put(thenBB.localNames.get(name),localName);
                    nextBB.localNames.put(name,localName);
                }
                if(!thenHasTerm)
                    thenBB.stats.add(new StatBranch(nextBB.num,thenLocalMap));
                if(!elseHasTerm)
                    lastBB.stats.add(new StatBranch(nextBB.num, localMap));
                currBB.next = nextBB.num;
                currBB = nextBB;
            }
            this.basicBlocks.add(thenBB.build());
            if(!elseHasTerm)
                this.basicBlocks.add(lastBB.build());
            this.localTypes = prevNames;
        }else if(stat instanceof StatementWhile while_){
            BasicBlockBuilder loopBB = new BasicBlockBuilder(nextBlock++);
            Map<Integer,Integer> intoLoopMap = new HashMap<>();
            Map<Integer,Integer> repeatLoopMap = new HashMap<>();
            Map<Integer,Integer> exitLoopMap = new HashMap<>();
            Map<String,Integer> atStartNames = new HashMap<>();
            Map<String,Type> prevNames = new HashMap<>(localTypes);
            for(String name : this.localTypes.keySet()){
                if(!currBB.localNames.containsKey(name))
                    continue;
                int localName = nextLocal++;
                loopBB.locals.put(localName,this.localTypes.get(name));
                intoLoopMap.put(currBB.localNames.get(name),localName);
                atStartNames.put(name,localName);
                loopBB.localNames.put(name,localName);
            }

            currBB.next = loopBB.num;

            BooleanExpr ctrl = while_.getControl();
            currBB.stats.add(new StatBranch(loopBB.num,intoLoopMap));
            BasicBlockBuilder lastBB = currBB;
            this.basicBlocks.add(lastBB.build());
            currBB = loopBB;
            SSAExpression left = convertExpr(ctrl.getLeft());
            SSAExpression right = convertExpr(ctrl.getRight());
            loopBB.stats.add(new StatNop()); // placeholder
            convertStatement(while_.getLooped());
            BasicBlockBuilder nextBB = new BasicBlockBuilder(nextBlock++);
            for(String name : prevNames.keySet()){
                if(!loopBB.localNames.containsKey(name))
                    continue;
                int localName = nextLocal++;
                nextBB.locals.put(localName,this.localTypes.get(name));
                exitLoopMap.put(atStartNames.get(name),localName);
                nextBB.localNames.put(name,localName);
            }
            loopBB.stats.set(0,new StatBranchCompare(nextBB.num,ctrl.getOperator().invert(),left,right,exitLoopMap));
            for(String name : prevNames.keySet()){
                if(!currBB.localNames.containsKey(name)||!atStartNames.containsKey(name))
                    continue;
                int localName = atStartNames.get(name);
                loopBB.locals.put(localName,this.localTypes.get(name));
                repeatLoopMap.put(currBB.localNames.get(name),localName);
            }
            currBB.stats.add(new StatBranch(loopBB.num,repeatLoopMap));
            currBB.next = nextBB.num;
            this.basicBlocks.add(currBB.build());
            currBB = nextBB;
            this.localTypes = prevNames;
        }else if(stat instanceof StatementDoWhile dowhile){
            BasicBlockBuilder loopBB = new BasicBlockBuilder(nextBlock++);
            Map<Integer,Integer> intoLoopMap = new HashMap<>();
            Map<Integer,Integer> repeatLoopMap = new HashMap<>();
            Map<Integer,Integer> exitLoopMap = new HashMap<>();
            Map<String,Integer> atStartNames = new HashMap<>();
            Map<String,Type> prevNames = new HashMap<>(this.localTypes);
            for(String name : this.localTypes.keySet()){
                if(!currBB.localNames.containsKey(name))
                    continue;
                int localName = nextLocal++;
                loopBB.locals.put(localName,this.localTypes.get(name));
                intoLoopMap.put(currBB.localNames.get(name),localName);
                atStartNames.put(name,localName);
                loopBB.localNames.put(name,localName);
            }

            currBB.next = loopBB.num;

            BooleanExpr ctrl = dowhile.getControl();
            currBB.stats.add(new StatBranch(loopBB.num,intoLoopMap));
            BasicBlockBuilder lastBB = currBB;
            this.basicBlocks.add(lastBB.build());
            currBB = loopBB;

            convertStatement(dowhile.getLooped());

            for(String name : prevNames.keySet()){
                if(!currBB.localNames.containsKey(name)||!atStartNames.containsKey(name))
                    continue;
                int localName = atStartNames.get(name);
                loopBB.locals.put(localName,this.localTypes.get(name));
                repeatLoopMap.put(currBB.localNames.get(name),localName);
            }
            SSAExpression left = convertExpr(ctrl.getLeft());
            SSAExpression right = convertExpr(ctrl.getRight());
            if(!currBB.hasTerminator()){
                BasicBlockBuilder nextBB = new BasicBlockBuilder(nextBlock++);
                for(String name : prevNames.keySet()) {
                    if (!loopBB.localNames.containsKey(name))
                        continue;
                    int localName = nextLocal++;
                    nextBB.locals.put(localName, this.localTypes.get(name));
                    exitLoopMap.put(loopBB.localNames.get(name), localName);
                    nextBB.localNames.put(name, localName);
                }
                currBB.stats.add(new StatBranchCompare(loopBB.num, ctrl.getOperator(),left,right,repeatLoopMap));
                currBB.stats.add(new StatBranch(nextBB.num,exitLoopMap));
                currBB.next = nextBB.num;
                this.basicBlocks.add(currBB.build());
                currBB = nextBB;
            }
            this.localTypes = prevNames;
        }else
            throw new ConversionError("Unrecognized statement "+ stat);
    }

    public SSAMethodDeclaration convertMethod(github.chorman0773.tiny.ast.MethodDeclaration method){
        List<Type> params = new ArrayList<>();
        nextLocal = 0;
        for(var param : method.getParameters()){
            this.localTypes.put(param.getName(),param.getType());
            int localName = nextLocal++;
            currBB.locals.put(localName,param.getType());
            currBB.localNames.put(param.getName(),localName);
            params.add(param.getType());
        }

        this.currSig = new MethodSignature(method.returnType(),params);

        for(var stat : method.getBlock().getStatements())
            convertStatement(stat);
        if(currBB.stats.size()==0||!currBB.stats.get(currBB.stats.size()-1).isTerminator()){
            if(method.isMain())
                currBB.stats.add(new StatReturn(new ExprInt(0)));
            else{
                throw new ConversionError("No return statement from function "+method.getName());
            }

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
