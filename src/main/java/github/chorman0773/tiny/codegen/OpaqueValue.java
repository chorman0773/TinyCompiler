package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.ast.Type;

public class OpaqueValue extends SSAValue{
    private final Type ty;
    private final ValLocation loc;

    public OpaqueValue(Type ty, ValLocation loc){
        this.ty = ty;
        this.loc = loc;
    }

    public Type type(){
        return ty;
    }

    public ValLocation location(){
        return loc;
    }
}
