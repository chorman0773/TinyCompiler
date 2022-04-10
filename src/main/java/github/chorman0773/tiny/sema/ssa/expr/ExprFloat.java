package github.chorman0773.tiny.sema.ssa.expr;

public class ExprFloat extends SSAExpression {
    private final double value;

    public ExprFloat(double value){
        this.value = value;
    }

    public double getValue(){
        return value;
    }

    public String toString(){
        return String.valueOf(value);
    }
}
