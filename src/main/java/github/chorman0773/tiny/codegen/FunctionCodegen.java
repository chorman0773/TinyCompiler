package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.BasicBlock;

import java.util.ArrayList;
import java.util.List;

public class FunctionCodegen<C extends RawCodegen> {

    private final C rawcg;

    List<SSAValue> localLocs;


    public FunctionCodegen(List<Type> params, C rawcg){
        List<SSAValue> paramLocs = new ArrayList<>();
        for(int i = 0;i<params.size();i++){
            paramLocs.add(new OpaqueValue(params.get(i),rawcg.findParameter(i)));
        }
        this.localLocs = paramLocs;
        this.rawcg = rawcg;
    }


    public void writeBasicBlock(BasicBlock bb){}
}
