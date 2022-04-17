package github.chorman0773.tiny.codegen;

import github.chorman0773.tiny.sema.ssa.SSAProgram;

import java.io.IOException;
import java.io.OutputStream;

public interface Codegen {

    public void writeIR(SSAProgram prg);

    public void writeOutput(OutputStream stream) throws IOException;
}
