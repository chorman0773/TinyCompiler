package github.chorman0773.tiny.opt.unused;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.opt.Optimizer;
import github.chorman0773.tiny.sema.ssa.BasicBlock;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.sema.ssa.expr.*;
import github.chorman0773.tiny.sema.ssa.stat.*;

import java.util.*;

public class RemoveUnused implements Optimizer {

    private void getUsedLocals(SSAExpression expr,Set<Integer> usedLocals){
        if(expr instanceof ExprLocal local)
            usedLocals.add(local.getLocalNumber());
        else if(expr instanceof ExprCast cast)
            getUsedLocals(cast.getExpr(),usedLocals);
        else if(expr instanceof ExprOp op){
            getUsedLocals(op.getLeft(),usedLocals);
            getUsedLocals(op.getRight(),usedLocals);
        }else if(expr instanceof ExprCall call)
            for(SSAExpression arg : call.getParameters())
                getUsedLocals(arg,usedLocals);
    }

    private void getUsedLocals(SSAStatement stat, Set<Integer> usedLocals){
        if(stat instanceof StatReturn ret)
            getUsedLocals(ret.getExpr(),usedLocals);
        else if(stat instanceof StatDeclaration decl)
            getUsedLocals(decl.getInitializer(),usedLocals);
        else if(stat instanceof StatWrite write)
            getUsedLocals(write.getExpr(),usedLocals);
        else if(stat instanceof StatBranchCompare cmp){
            getUsedLocals(cmp.getLeft(),usedLocals);
            getUsedLocals(cmp.getRight(),usedLocals);
            usedLocals.addAll(cmp.getRemaps().keySet());
        }else if(stat instanceof StatBranch branch)
            usedLocals.addAll(branch.getRemaps().keySet());
        else if(stat instanceof StatDiscard disc)
            getUsedLocals(disc.inner(),usedLocals);
    }

    private SSAMethodDeclaration removeUnusedLocals(SSAMethodDeclaration decl){
        List<BasicBlock> blocks = decl.getBlocks();
        while(true){
            boolean changed = false;
            Set<Integer> usedLocals = new HashSet<>();
            for(BasicBlock bb :  blocks){
                for(SSAStatement stat : bb.getStats())
                    getUsedLocals(stat,usedLocals);
            }
            List<BasicBlock> newBlocks = new ArrayList<>();
            for(BasicBlock bb :  blocks){
                List<SSAStatement> newStats = new ArrayList<>();
                for(SSAStatement stat : bb.getStats()){
                    if(stat instanceof StatDeclaration dec){
                        if(!usedLocals.contains(dec.getLocalNum())){
                            if(dec.getInitializer().hasSideEffects())
                                newStats.add(new StatDiscard(dec.getInitializer()));
                            changed = true;
                        }
                    }else if(stat instanceof StatBranchCompare cmp){
                        Map<Integer,Integer> newRemaps = new HashMap<>();
                        for(var remap : cmp.getRemaps().entrySet()){
                            if(usedLocals.contains(remap.getValue()))
                                newRemaps.put(remap.getKey(),remap.getValue());
                            else
                                changed = true;
                        }

                        newStats.add(new StatBranchCompare(cmp.getTargetNumber(),cmp.getOp(),cmp.getLeft(),cmp.getRight(),newRemaps));
                    }else if(stat instanceof StatBranch br){
                        Map<Integer,Integer> newRemaps = new HashMap<>();
                        for(var remap : br.getRemaps().entrySet()){
                            if(usedLocals.contains(remap.getValue()))
                                newRemaps.put(remap.getKey(),remap.getValue());
                            else
                                changed = true;
                        }

                        newStats.add(new StatBranch(br.getTargetNumber(),newRemaps));
                    }else
                        newStats.add(stat);
                }
                Map<Integer, Type> locals = new HashMap<>();
                for(var local : bb.getLocals().entrySet()){
                    if(usedLocals.contains(local.getKey()))
                        locals.put(local.getKey(),local.getValue());
                }
                newBlocks.add(new BasicBlock(bb.getNum(),bb.getNext(),locals,newStats));
            }
            blocks = newBlocks;

            if(!changed)
                break;

        }
        return new SSAMethodDeclaration(decl.getParameters(),decl.isMain(),decl.getName(),decl.getReturnType(),blocks);
    }


    @Override
    public SSAProgram optimize(SSAProgram prg) {
        return new SSAProgram(prg.getDeclarations().stream().map(this::removeUnusedLocals).toList());
    }

    @Override
    public String getName() {
        return "remove-unused";
    }
}
