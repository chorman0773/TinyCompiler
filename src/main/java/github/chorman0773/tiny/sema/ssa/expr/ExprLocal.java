package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprLocal exprLocal = (ExprLocal) o;
        return localN == exprLocal.localN;
    }

    @Override
    public int hashCode() {
        return Objects.hash(localN);
    }

    @Override
    public boolean isDefinatelyEqual(SSAExpression other) {
        return this.equals(other);
    }
}
