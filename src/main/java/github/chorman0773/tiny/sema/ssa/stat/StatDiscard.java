package github.chorman0773.tiny.sema.ssa.stat;

import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class StatDiscard extends SSAStatement{
    private final SSAExpression expr;

    public StatDiscard(SSAExpression expr) {
        this.expr = expr;
    }

    public SSAExpression inner(){
        return expr;
    }

    public String toString(){
        return "discard("+expr+")";
    }
}
