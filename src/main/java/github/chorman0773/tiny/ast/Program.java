package github.chorman0773.tiny.ast;

import java.util.Collections;
import java.util.List;

public class Program {
    private List<TopLevelDeclaration> decls;

    public Program(List<TopLevelDeclaration> decls){
        this.decls = decls;
    }

    public List<TopLevelDeclaration> getDeclarations(){
        return Collections.unmodifiableList(decls);
    }

    public String toString(){
        return decls.toString();
    }
}
