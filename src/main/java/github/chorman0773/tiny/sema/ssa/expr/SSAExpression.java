package github.chorman0773.tiny.sema.ssa.expr;



public abstract class SSAExpression {

    public boolean isDefinatelyEqual(SSAExpression other){
        return false;
    }

    public boolean hasSideEffects(){
        return false;
    }
}
