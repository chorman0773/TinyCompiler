package github.chorman0773.tiny.opt.fold;

import github.chorman0773.tiny.sema.ssa.expr.ExprFloat;
import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class TransparentFloat extends SSAValue{
    private double val;

    public TransparentFloat(double val){
        this.val = val;
    }

    public TransparentFloat(ExprFloat i){
        this.val = i.getValue();
    }

    public double getValue(){
        return this.val;
    }

    public void setValue(double val){
        this.val = val;
    }

    public SSAExpression expression(){
        return new ExprFloat(val);
    }

    public boolean isTransparent(){
        return true;
    }
}
