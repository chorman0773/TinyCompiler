package github.chorman0773.tiny.ast;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionCall extends Expression{
    private String name;
    private List<Expression> parameters;

    public ExpressionCall(String method, List<Expression> parameters){
        this.name = method;
        this.parameters = parameters;
    }


    public List<Expression> getParameters(){
        return Collections.unmodifiableList(parameters);
    }

    public String getMethodName(){
        return name;
    }


    public String toString(){
        return name + "(" + parameters.stream().map(Expression::toString).collect(Collectors.joining(",")) +  ")";
    }
}
