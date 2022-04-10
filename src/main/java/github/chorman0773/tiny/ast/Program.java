package github.chorman0773.tiny.ast;

import java.util.Collections;
import java.util.List;

public class Program {
    private List<MethodDeclaration> decls;

    public Program(List<MethodDeclaration> decls){
        this.decls = decls;
    }

    public List<MethodDeclaration> getDeclarations(){
        return Collections.unmodifiableList(decls);
    }

    public String toString(){
        return decls.toString();
    }
}
