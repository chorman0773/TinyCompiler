package github.chorman0773.tiny.sema.ssa.stat;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

public class StatDeclaration extends SSAStatement {
    private final Type ty;
    private final int localN;
    private final SSAExpression expr;

    public StatDeclaration(Type ty, int localN, SSAExpression expr){
        this.ty = ty;
        this.localN = localN;
        this.expr = expr;
    }

    public Type getType(){
        return ty;
    }

    public int getLocalNum(){
        return localN;
    }

    public SSAExpression getInitializer(){
        return expr;
    }

    public String toString(){
        return "_"+localN+": "+ty+" = "+expr;
    }
}
