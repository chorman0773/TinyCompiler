package github.chorman0773.tiny.ast;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MethodDeclaration {
    private final boolean isMain;
    private final Type retTy;
    private final String name;
    private final List<Parameter> parameters;
    private final Block block;

    public MethodDeclaration(boolean isMain, Type retTy, String name, List<Parameter> parameters, Block block){
        this.isMain = isMain;
        this.retTy = retTy;
        this.name = name;
        this.parameters = parameters;
        this.block = block;
    }

    public boolean isMain(){
        return isMain;
    }

    public Type returnType(){
        return retTy;
    }

    public String getName(){
        return name;
    }

    public List<Parameter> getParameters(){
        return Collections.unmodifiableList(this.parameters);
    }

    public Block getBlock(){
        return this.block;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(retTy).append(' ');
        if(isMain)
            builder.append("MAIN ");
        builder.append(name);
        builder.append("(");
        builder.append(this.parameters.stream().map(Parameter::toString).collect(Collectors.joining(",")));
        builder.append(") ");
        builder.append(block);
        return builder.toString();
    }
}
