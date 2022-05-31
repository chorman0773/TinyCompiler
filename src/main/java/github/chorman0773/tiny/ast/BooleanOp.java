package github.chorman0773.tiny.ast;

public enum BooleanOp {
    CmpEq,
    CmpNe;

    public String toString(){
        return switch(this){
            case CmpEq -> "==";
            case CmpNe -> "!=";
        };
    }

    public BooleanOp invert(){
        return switch(this){
            case CmpEq -> CmpNe;
            case CmpNe -> CmpEq;
        };
    }
}
