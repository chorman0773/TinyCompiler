package github.chorman0773.tiny.ast;

public enum BinaryOp {
    Add,
    Sub,
    Mul,
    Div;

    public String toString(){
        return switch(this){
            case Add -> "+";
            case Sub -> "-";
            case Mul -> "*";
            case Div -> "/";
        };
    }
}
