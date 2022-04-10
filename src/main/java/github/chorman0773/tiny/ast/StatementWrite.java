package github.chorman0773.tiny.ast;

public class StatementWrite extends Statement{
    private final Expression value;
    private final String path;

    public StatementWrite(Expression value, String path){
        this.value = value;
        this.path = path;
    }

    public Expression getValue(){
        return this.value;
    }

    public String getPath(){
        return this.path;
    }

    public String toString(){
        return "WRITE("+value+",\""+path+"\")";
    }
}
