package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.codegen.SSAValue;

public class TransparentFloat extends SSAValue {
    private final double value;

    public TransparentFloat(double value){
        this.value = value;
    }

    public double value(){
        return value;
    }
}
