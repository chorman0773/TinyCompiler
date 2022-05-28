package github.chorman0773.tiny.opt;

import github.chorman0773.tiny.sema.ssa.SSAProgram;

public interface Optimizer {
    public SSAProgram optimize(SSAProgram prg);

    public String getName();
}
