package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Identifier;
import github.chorman0773.tiny.sema.ssa.MethodSignature;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExprCall extends SSAExpression {
    private final Identifier name;
    private final List<SSAExpression> params;
    private final MethodSignature callSig;

    public ExprCall(Identifier name, List<SSAExpression> params, MethodSignature sig){
        this.name = name;
        this.params = params;
        this.callSig = sig;
    }

    public Identifier getFunction(){
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprCall exprCall = (ExprCall) o;
        return Objects.equals(name, exprCall.name) && Objects.equals(params, exprCall.params) && Objects.equals(callSig, exprCall.callSig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, params, callSig);
    }

}
