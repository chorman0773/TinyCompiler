package github.chorman0773.tiny.ast;

public class ExpressionId extends Expression{
    private final String id;

    public ExpressionId(String id){
        this.id = id;
    }

    public String toString(){
        return "Id("+this.id+")";
    }

    public String getIdentifier(){
        return id;
    }
}
