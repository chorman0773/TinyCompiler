package github.chorman0773.tiny.opt.inline;

import github.chorman0773.tiny.opt.Optimizer;
import github.chorman0773.tiny.sema.ssa.SSAProgram;

public class Inliner implements Optimizer {
    @Override
    public SSAProgram optimize(SSAProgram prg) {
        return prg;
    }

    public String toString(){
        return "inliner";
    }
}
