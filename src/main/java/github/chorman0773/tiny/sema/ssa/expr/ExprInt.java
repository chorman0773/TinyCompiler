package github.chorman0773.tiny.sema.ssa.expr;

import java.util.Objects;

public class ExprInt extends SSAExpression {
    private final int value;

    public ExprInt(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public String toString(){
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprInt exprInt = (ExprInt) o;
        return value == exprInt.value;
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
