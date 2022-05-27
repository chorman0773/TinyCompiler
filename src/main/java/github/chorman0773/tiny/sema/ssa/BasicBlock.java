package github.chorman0773.tiny.sema.ssa;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.stat.SSAStatement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BasicBlock {
    private final int num;
    private final int next;
    private final Map<Integer,Type> locals;
    private final List<SSAStatement> stats;

    public BasicBlock(int num, int next, Map<Integer, Type> locals, List<SSAStatement> stats){
        this.num = num;
        this.next =next;
        this.locals = locals;
        this.stats = stats;
    }

    public int getNum() {
        return num;
    }

    public int getNext() {
        return next;
    }

    public Map<Integer,Type> getLocals() {
        return locals;
    }

    public List<SSAStatement> getStats() {
        return stats;
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
