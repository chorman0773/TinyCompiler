package github.chorman0773.tiny.parse;

import github.chorman0773.tiny.ast.BinaryOp;

public record OperatorTuple(BinaryOp op,int lbp, int rbp){}
