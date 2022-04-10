package github.chorman0773.tiny.sema.ssa.stat;

import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class StatReturn extends SSAStatement {
    private final SSAExpression expr;
    public StatReturn(SSAExpression expr){
        this.expr = expr;
    }

    public SSAExpression getExpr(){
        return expr;
    }

    public String toString(){
        return "return "+expr;
    }
}
