package github.chorman0773.tiny.ast;

import java.util.Optional;

public class StatementDeclaration extends Statement{
    private final Type ty;
    private final String name;
    private final Expression init;

    public StatementDeclaration(Type ty, String name, Expression init){
        this.ty = ty;
        this.name = name;
        this.init = init;
    }

    public Type getType(){
        return ty;
    }

    public String getName(){
        return name;
    }

    public Optional<Expression> getInitializer(){
        return Optional.ofNullable(init);
    }

    public String toString(){
        StringBuilder build = new StringBuilder();
        build.append(ty);
        build.append(' ');
        build.append(name);
        if(init!=null)
            build.append(" := ").append(init);
        return build.toString();
    }
}
