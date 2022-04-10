package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

public class ExprRead extends SSAExpression {
    private final String path;
    private final Type ty;
    public ExprRead(Type ty,String path){
        this.ty = ty;
        this.path = path;
    }

    public Type getType(){
        return this.ty;
    }

    public String getPath(){
        return this.path;
    }

    public String toString(){
        return "READ(\""+path+"\") as "+ty;
    }

}
