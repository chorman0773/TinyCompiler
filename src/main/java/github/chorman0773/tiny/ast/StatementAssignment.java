package github.chorman0773.tiny.ast;

public class StatementAssignment extends Statement {
    private final String name;
    private final Expression value;

    public StatementAssignment(String id, Expression value){
        this.name = id;
        this.value = value;
    }

    public String getIdent(){
        return name;
    }

    public Expression getValue(){
        return value;
    }

    public String toString(){
        return name + ":=" + value;
    }
}
