package github.chorman0773.tiny.ast;

public class ExpressionNumber extends Expression {
    private final double value;

    public ExpressionNumber(double value){
        this.value = value;
    }

    public double getValue(){
        return this.value;
    }

    public String toString(){
        return String.valueOf(value);
    }
}
