package github.chorman0773.tiny.opt.fold;

import github.chorman0773.tiny.opt.Optimizer;
import github.chorman0773.tiny.sema.ssa.BasicBlock;
import github.chorman0773.tiny.sema.ssa.SSAConverter;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.sema.ssa.expr.*;
import github.chorman0773.tiny.sema.ssa.stat.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantFolder implements Optimizer {
    private Map<Integer,SSAValue> locals;
    private SSAValue convertExpr(SSAExpression expr){
        if(expr instanceof ExprInt i)
            return new TransparentInt(i);
        else if(expr instanceof ExprFloat f)
            return new TransparentFloat(f);
        else if(expr instanceof ExprCast c){
            SSAValue val = convertExpr(c.getExpr());
            if(val instanceof TransparentInt i){
                return switch(c.getType()){
                    case Int -> i;
                    case Real -> new TransparentFloat((double)i.getValue());
                    default -> new OpaqueValue(new ExprCast(c.getType(),val.expression()));
                };
            }else if(val instanceof TransparentFloat f){
                return switch(c.getType()){
                    case Int -> new TransparentInt((int)f.getValue());
                    case Real -> f;
                    default -> new OpaqueValue(new ExprCast(c.getType(),val.expression()));
                };
            }else{
                return new OpaqueValue(new ExprCast(c.getType(),val.expression()));
            }
        }else if(expr instanceof ExprOp op){
            SSAValue left = convertExpr(op.getLeft());
            SSAValue right = convertExpr(op.getRight());
            if(left instanceof TransparentInt li&&right instanceof TransparentInt lr){
                int newval = switch(op.getBinaryOp()){
                    case Add -> li.getValue()+lr.getValue();
                    case Sub -> li.getValue()-lr.getValue();
                    case Mul -> li.getValue()*lr.getValue();
                    case Div -> li.getValue()/lr.getValue();
                };
                return new TransparentInt(newval);
            }else if(left instanceof TransparentFloat li&&right instanceof TransparentFloat lr){
                double newval = switch(op.getBinaryOp()){
                    case Add -> li.getValue()+lr.getValue();
                    case Sub -> li.getValue()-lr.getValue();
                    case Mul -> li.getValue()*lr.getValue();
                    case Div -> li.getValue()/lr.getValue();
                };
                return new TransparentFloat(newval);
            }else
                return new OpaqueValue(new ExprOp(op.getBinaryOp(),left.expression(),right.expression()));
        }else if(expr instanceof ExprLocal n){
            return locals.get(n.getLocalNumber());
        }else
            return new OpaqueValue(expr);
    }

    private SSAStatement convertStatement(SSAStatement stat){
        if(stat instanceof StatDeclaration decl){
            SSAValue val = convertExpr(decl.getInitializer());
            if(val instanceof OpaqueValue)
                locals.put(decl.getLocalNum(),new OpaqueValue(new ExprLocal(decl.getLocalNum())));
            else
                locals.put(decl.getLocalNum(),val);
            return new StatDeclaration(decl.getType(),decl.getLocalNum(),val.expression());
        }else if(stat instanceof StatWrite write){
            SSAValue val = convertExpr(write.getExpr());
            return new StatWrite(val.expression(),write.getPath());
        }else if(stat instanceof StatStoreDead storedead){
            locals.remove(storedead.getLocalNumber());
            return storedead;
        }else if(stat instanceof StatBranchCompare bcmp){
            SSAValue left = convertExpr(bcmp.getLeft());
            SSAValue right = convertExpr(bcmp.getRight());
            boolean definatelyEquals = left.expression().isDefinatelyEqual(right.expression());
            boolean definatelyUnequal = (!definatelyEquals)&&left.isTransparent()&&right.isTransparent();
            for(int i : bcmp.getRemaps().values())
                this.locals.put(i,new OpaqueValue(new ExprLocal(i)));
            switch(bcmp.getOp()) {
                case CmpEq -> {
                    if (definatelyEquals)
                        return new StatBranch(bcmp.getTargetNumber(), bcmp.getRemaps());
                    else if (definatelyUnequal)
                        return new StatNop();
                    else
                        return new StatBranchCompare(bcmp.getTargetNumber(), bcmp.getOp(), left.expression(), right.expression(), bcmp.getRemaps());
                }
                case CmpNe -> {
                    if (definatelyEquals)
                        return new StatNop();
                    else if (definatelyUnequal)
                        return new StatBranch(bcmp.getTargetNumber(), bcmp.getRemaps());
                    else
                        return new StatBranchCompare(bcmp.getTargetNumber(), bcmp.getOp(), left.expression(), right.expression(), bcmp.getRemaps());
                }
            }
        }else if(stat instanceof StatReturn ret)
            return new StatReturn(convertExpr(ret.getExpr()).expression());
        else if(stat instanceof StatBranch b){
            for(int i : b.getRemaps().values())
                this.locals.put(i,new OpaqueValue(new ExprLocal(i)));
            return b;
        }
        else
            return stat;
        throw new Error("Unreachable code");
    }

    private BasicBlock convertBB(BasicBlock bb){
        List<SSAStatement> stats = new ArrayList<>();
        for(var stat : bb.getStats()){
            var convertedStat = convertStatement(stat);
            stats.add(convertedStat);
            if(convertedStat.isTerminator())
                break; // Since Some branch compares (which aren't terminators) can become unconditional branches
        }
        return new BasicBlock(bb.getNum(),bb.getNext(),bb.getLocals(),stats);
    }

    private SSAMethodDeclaration convertMethod(SSAMethodDeclaration decl){
        this.locals.clear();
        int paramNo = 0;
        for(var ignored : decl.getParameters()){
            int local = paramNo++;
            this.locals.put(local,new OpaqueValue(new ExprLocal(local)));
        }
        List<BasicBlock> retBBs = new ArrayList<>();
        for(BasicBlock bb : decl.getBlocks())
            retBBs.add(convertBB(bb));
        return new SSAMethodDeclaration(decl.getParameters(),decl.isMain(),decl.getName(),decl.getReturnType(),retBBs);
    }
    
    @Override
    public SSAProgram optimize(SSAProgram prg) {
        this.locals = new HashMap<>();
        List<SSAMethodDeclaration> decls = prg.getDeclarations().stream().map(this::convertMethod).toList();
        return new SSAProgram(decls);
    }

    public String toString(){
        return "constant-fold";
    }
}
