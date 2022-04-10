package github.chorman0773.tiny.sema.ssa.stat;

import github.chorman0773.tiny.ast.BooleanOp;
import github.chorman0773.tiny.sema.ssa.expr.SSAExpression;

import java.util.Collections;
import java.util.Map;

public class StatBranchCompare extends SSAStatement {
    private final int targetN;
    private final BooleanOp op;
    private final SSAExpression left;
    private final SSAExpression right;
    private final Map<Integer, Integer> remapLocals;

    public StatBranchCompare(int targetN, BooleanOp op, SSAExpression left, SSAExpression right, Map<Integer,Integer> remapLocals){
        this.targetN = targetN;
        this.op = op;
        this.left = left;
        this.right = right;
        this.remapLocals = remapLocals;
    }

    public int getTargetNumber(){
        return this.targetN;
    }

    public BooleanOp getOp(){
        return this.op;
    }

    public SSAExpression getLeft(){
        return this.left;
    }

    public SSAExpression getRight(){
        return this.right;
    }

    public Map<Integer, Integer> getRemaps(){
        return Collections.unmodifiableMap(remapLocals);
    }

    public String toString(){
        StringBuilder st = new StringBuilder();
        st.append("branch compare (").append(left).append(" ").append(op).append(" ").append(right).append(") @").append(targetN);
        st.append(" [");
        String sep = "";
        for(Map.Entry<Integer,Integer> remap : remapLocals.entrySet()){
            st.append(sep).append(remap.getKey()).append(" => ").append(remap.getValue());
            sep = ", ";
        }
        st.append("]");
        return st.toString();
    }
}
