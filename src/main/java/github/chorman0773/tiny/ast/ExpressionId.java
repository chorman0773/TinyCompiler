package github.chorman0773.tiny.ast;

import github.chorman0773.tiny.lex.Span;

public class ExpressionId extends Expression{
    private final Identifier id;

    public ExpressionId(Identifier id){
        this.id = id;
    }

    public String toString(){
        return "Id("+this.id+")";
    }

    public Identifier getIdentifier(){
        return id;
    }
}
