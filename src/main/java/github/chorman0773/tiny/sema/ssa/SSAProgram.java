package github.chorman0773.tiny.sema.ssa;


import java.util.Collections;
import java.util.List;

public class SSAProgram {
    private final List<SSAMethodDeclaration> decls;

    public SSAProgram(List<SSAMethodDeclaration> decls){
        this.decls = decls;
    }

    public List<SSAMethodDeclaration> getDeclarations(){
        return Collections.unmodifiableList(decls);
    }

    public String toString(){
        return decls.toString();
    }
}
