package github.chorman0773.tiny.ast;

public class BooleanExpr {
    private final BooleanOp op;
    private final Expression left;
    private final Expression right;

    public BooleanExpr(BooleanOp op, Expression left, Expression right){
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public BooleanOp getOperator(){
        return op;
    }

    public Expression getLeft(){
        return left;
    }

    public Expression getRight(){
        return right;
    }

    public String toString(){
        return left + " " + op + " " + right;
    }
}
