package github.chorman0773.tiny.ast;

import github.chorman0773.tiny.lex.Span;

public class StatementAssignment extends Statement {
    private final Identifier name;
    private final Expression value;

    public StatementAssignment(Identifier id, Expression value){
        this.name = id;
        this.value = value;
    }

    public Identifier getIdent(){
        return name;
    }

    public Expression getValue(){
        return value;
    }

    public String toString(){
        return name +":=" + value;
    }
}
