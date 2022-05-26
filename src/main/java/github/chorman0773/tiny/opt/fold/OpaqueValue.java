package github.chorman0773.tiny.opt.fold;

import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class OpaqueValue extends SSAValue {
    private final SSAExpression expr;
    OpaqueValue(SSAExpression expr){
        this.expr = expr;
    }
    
    @Override
    public SSAExpression expression() {
        return expr;
    }
}
