package github.chorman0773.tiny.ast;

public class ExpressionBinary extends Expression {
    private final BinaryOp operator;
    private final Expression right;
    private final Expression left;

    public ExpressionBinary(BinaryOp op, Expression l, Expression r){
        this.operator = op;
        this.left = l;
        this.right = r;
    }

    public BinaryOp getOperator(){
        return operator;
    }

    public Expression getLeft(){
        return left;
    }

    public Expression getRight(){
        return right;
    }

    public String toString(){
        return left + " " + operator + " " + right;
    }
}
