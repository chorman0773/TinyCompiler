package github.chorman0773.tiny.ast;

public class StatementReturn extends Statement{
    private final Expression value;
    public StatementReturn(Expression value){
        this.value = value;
    }

    public Expression getValue(){
        return value;
    }

    public String toString(){
        return "RETURN "+value;
    }
}
