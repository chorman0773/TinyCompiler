package github.chorman0773.tiny.sema.ssa.expr;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprFloat exprFloat = (ExprFloat) o;
        return Double.compare(exprFloat.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean isDefinatelyEqual(SSAExpression other) {
        return this.equals(other);
    }
}
