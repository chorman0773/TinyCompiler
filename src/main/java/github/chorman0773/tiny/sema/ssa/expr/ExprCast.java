package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

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
}
