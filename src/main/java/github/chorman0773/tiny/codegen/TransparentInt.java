package github.chorman0773.tiny.codegen;

public class TransparentInt extends SSAValue{
    private final int value;

    public TransparentInt(int value){
        this.value = value;
    }

    public int value(){
        return value;
    }
}
