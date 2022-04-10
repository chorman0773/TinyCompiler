package github.chorman0773.tiny.ast;

public class StatementRead extends Statement{
    private final String id;
    private final String path;

    public StatementRead(String id, String path){
        this.id = id;
        this.path = path;
    }

    public String getIdent(){
        return this.id;
    }

    public String getPath(){
        return this.path;
    }

    public String toString(){
        return "READ("+id+",\""+path+"\")";
    }
}
