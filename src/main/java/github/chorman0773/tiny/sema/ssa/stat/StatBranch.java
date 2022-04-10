package github.chorman0773.tiny.sema.ssa.stat;

import java.util.Collections;
import java.util.Map;

public class StatBranch extends SSAStatement {
    private final int targetN;
    private final Map<Integer, Integer> remapLocals;

    public StatBranch(int targetN,Map<Integer, Integer> remapLocals){
        this.targetN = targetN;
        this.remapLocals = remapLocals;
    }

    public int getTargetNumber(){
        return this.targetN;
    }

    public Map<Integer, Integer> getRemaps(){
        return Collections.unmodifiableMap(remapLocals);
    }

    public String toString(){
        StringBuilder st = new StringBuilder();
        st.append("branch @").append(targetN);
        st.append(" [");
        String sep = "";
        for(Map.Entry<Integer,Integer> remap : remapLocals.entrySet()){
            st.append(sep).append(remap.getKey()).append(" => ").append(remap.getValue());
            sep = ", ";
        }
        st.append("]");
        return st.toString();
    }

    @Override
    public boolean isTerminator() {
        return true;
    }
}
