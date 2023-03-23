package github.chorman0773.tiny.ast;

public class ImportDecl extends TopLevelDeclaration {
    public ImportDecl(String name) {
        super(name);
    }

    @Override
    public String toString(){
        return "IMPORT "+super.getName();
    }
}
