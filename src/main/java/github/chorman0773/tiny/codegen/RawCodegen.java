package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.ast.BinaryOp;
import github.chorman0773.tiny.ast.BooleanOp;
import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.MethodSignature;

public interface RawCodegen {

    public ValLocation findParameter(int paramN);

    public ValLocation allocateLocal(Type ty);

    public void freeLocal(ValLocation loc);

    public void markBBStart(int bb);

    public void branchToBB(int bb);

    public void branchCompareTo(int bb, ValLocation left, ValLocation right, BooleanOp op);

    public void branchCompareToConst(int bb,ValLocation left,double right, BooleanOp op);
    public void branchCompareToConst(int bb,ValLocation left,int right, BooleanOp op);

    public void moveConstTo(ValLocation dest, double val);
    public void moveConstTo(ValLocation dest, int val);

    public void moveVal(ValLocation dest, ValLocation src, Type ty);

    public void callFunction(String name, MethodSignature sig, SSAValue[] vals, ValLocation store);

    public void read(ValLocation store, String file,Type asty);

    public void write(ValLocation in, String file, Type inTy);

    public void castInto(ValLocation dest, ValLocation src, Type destTy, Type srcTy);

    public void computeBinaryOp(ValLocation into, ValLocation left, ValLocation right, Type ty, BinaryOp op);

    public void computeBinaryOpConst(ValLocation into, ValLocation left, int right,Type ty, BinaryOp op);
    public void computeBinaryOpConst(ValLocation into, ValLocation left, double right,Type ty, BinaryOp op);
    public void computeBinaryOpConst(ValLocation into, int left, ValLocation right,Type ty, BinaryOp op);
    public void computeBinaryOpConst(ValLocation into, double left, ValLocation right,Type ty, BinaryOp op);
}
