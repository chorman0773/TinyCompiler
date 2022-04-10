package github.chorman0773.tiny.ast;

public class ParenExpr extends Expression {
    private final Expression inner;

    public ParenExpr(Expression inner){
        this.inner = inner;
    }

    public Expression getInner(){
        return this.inner;
    }

    public String toString(){
        return "("+inner+")";
    }
}
