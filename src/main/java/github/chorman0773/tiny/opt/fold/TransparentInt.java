package github.chorman0773.tiny.opt.fold;

import github.chorman0773.tiny.sema.ssa.expr.ExprInt;
import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class TransparentInt extends SSAValue {
    private int val;

    public TransparentInt(int val){
        this.val = val;
    }

    public TransparentInt(ExprInt i){
        this.val = i.getValue();
    }

    public int getValue(){
        return this.val;
    }

    public void setValue(int val){
        this.val = val;
    }

    public SSAExpression expression(){
        return new ExprInt(val);
    }

    public boolean isTransparent(){
        return true;
    }
}
