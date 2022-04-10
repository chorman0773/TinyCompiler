package github.chorman0773.tiny.sema.ssa.stat;

import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class StatWrite extends SSAStatement {
    private final SSAExpression expr;
    private final String path;

    public StatWrite(SSAExpression expr, String path){
        this.expr = expr;
        this.path = path;
    }

    public SSAExpression getExpr(){
        return expr;
    }

    public String getPath(){
        return path;
    }

    public String toString(){
        return "write("+expr+", \""+path+"\")";
    }
}
