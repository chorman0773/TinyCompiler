package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

import java.util.Objects;

public class ExprCast extends SSAExpression {
    private final Type ty;
    private final SSAExpression cast;

    public ExprCast(Type ty, SSAExpression expr){
        this.ty = ty;
        this.cast = expr;
    }

    public Type getType(){
        return ty;
    }

    public SSAExpression getExpr(){
        return cast;
    }

    public String toString(){
        return cast + " as " + ty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprCast exprCast = (ExprCast) o;
        return ty == exprCast.ty && Objects.equals(cast, exprCast.cast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ty, cast);
    }

    @Override
    public boolean isDefinatelyEqual(SSAExpression other){
        if(other instanceof ExprCast cast)
            return ty == cast.ty&& this.cast.isDefinatelyEqual(cast.cast);
        else
            return false;
    }

    public boolean hasSideEffects(){
        return cast.hasSideEffects();
    }
}
