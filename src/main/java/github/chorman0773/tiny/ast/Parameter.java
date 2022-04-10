package github.chorman0773.tiny.ast;

public class Parameter {
    private final Type ty;
    private final String name;

    public Parameter(Type ty, String name){
        this.ty = ty;
        this.name = name;
    }

    public Type getType(){
        return this.ty;
    }

    public String getName(){
        return this.name;
    }


    public String toString(){
        return ty+" "+name;
    }
}
