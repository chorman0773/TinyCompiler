package github.chorman0773.tiny.sema.ssa.expr;

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
}
