package github.chorman0773.tiny.ast;

public class ExpressionString extends Expression{
    private final String lit;

    public ExpressionString(String lit){
        this.lit = lit;
    }

    public String getLiteral(){
        return lit;
    }
}
