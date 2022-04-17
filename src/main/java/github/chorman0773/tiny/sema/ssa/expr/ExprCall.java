package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.sema.MethodSignature;

import java.util.Collections;
import java.util.List;

public class ExprCall extends SSAExpression {
    private final String name;
    private final List<SSAExpression> params;
    private final MethodSignature callSig;

    public ExprCall(String name, List<SSAExpression> params,MethodSignature sig){
        this.name = name;
        this.params = params;
        this.callSig = sig;
    }

    public String getFunction(){
        return name;
    }

    public List<SSAExpression> getParameters(){
        return Collections.unmodifiableList(params);
    }

    public MethodSignature getSignature(){
        return callSig;
    }

    public String toString(){
        StringBuilder st = new StringBuilder();
        String sep = "";
        st.append(name).append("(");
        for(var expr : params){
            st.append(sep).append(expr);
            sep = ", ";
        }
        st.append(")");
        return st.toString();
    }

}
