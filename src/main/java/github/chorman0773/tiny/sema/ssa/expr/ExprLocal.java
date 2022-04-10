package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

public class ExprLocal extends SSAExpression {
    private final int localN;

    public ExprLocal(int localN){
        this.localN = localN;
    }

    public int getLocalNumber(){
        return localN;
    }

    public String toString(){
        return "_"+localN;
    }
}
