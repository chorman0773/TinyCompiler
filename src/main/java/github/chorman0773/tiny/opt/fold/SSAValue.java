package github.chorman0773.tiny.opt.fold;

import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public abstract class SSAValue {
    public abstract SSAExpression expression();

    public boolean isTransparent(){
        return false;
    }
}
