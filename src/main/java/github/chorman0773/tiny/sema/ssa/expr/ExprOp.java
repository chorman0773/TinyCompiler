package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.BinaryOp;
import github.chorman0773.tiny.ast.Type;

import java.util.Objects;

public class ExprOp extends SSAExpression {
    private final BinaryOp op;
    private final SSAExpression left;
    private final SSAExpression right;

    public ExprOp(BinaryOp op, SSAExpression l, SSAExpression r){
        this.op = op;
        this.left = l;
        this.right = r;
    }

    public BinaryOp getBinaryOp(){
        return op;
    }

    public SSAExpression getLeft(){
        return left;
    }

    public SSAExpression getRight(){
        return right;
    }

    public String toString(){
        return "("+left+" "+op+" "+right+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprOp exprOp = (ExprOp) o;
        return op == exprOp.op && Objects.equals(left, exprOp.left) && Objects.equals(right, exprOp.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, left, right);
    }

    @Override
    public boolean isDefinatelyEqual(SSAExpression other) {
        if(other instanceof ExprOp op)
            return this.op==op.op&&this.left.isDefinatelyEqual(op.left)&&this.right.isDefinatelyEqual(op.right);
        else
            return false;
    }

    public boolean hasSideEffects(){
        return this.left.hasSideEffects()||this.right.hasSideEffects();
    }
}
