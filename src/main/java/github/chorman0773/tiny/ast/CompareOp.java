package github.chorman0773.tiny.ast;

public enum CompareOp {
    CmpEq,
    CmpNe,
    CmpLt,
    CmpGt,
    CmpLe,
    CmpGe;

    public String toString(){
        return switch(this){
            case CmpEq -> "==";
            case CmpNe -> "!=";
            case CmpLt -> "<";
            case CmpLe -> "<=";
            case CmpGt -> ">";
            case CmpGe -> ">=";
        };
    }

    public CompareOp invert(){
        return switch(this){
            case CmpEq -> CmpNe;
            case CmpNe -> CmpEq;
            case CmpLt -> CmpGe;
            case CmpLe -> CmpGt;
            case CmpGt -> CmpLe;
            case CmpGe -> CmpLt;
        };
    }
}
