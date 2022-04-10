package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.BinaryOp;
import github.chorman0773.tiny.ast.Type;

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

}
