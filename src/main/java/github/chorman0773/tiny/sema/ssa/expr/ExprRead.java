package github.chorman0773.tiny.sema.ssa.expr;

import github.chorman0773.tiny.ast.Type;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprRead exprRead = (ExprRead) o;
        return Objects.equals(path, exprRead.path) && ty == exprRead.ty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, ty);
    }

}
