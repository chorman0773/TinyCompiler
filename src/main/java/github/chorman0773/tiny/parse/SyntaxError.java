package github.chorman0773.tiny.parse;

public class SyntaxError extends Exception{
//    private final Diagnostic diag;
    public SyntaxError(String diag) {
        super(diag);
    }

//    public Diagnostic getDiagnostic(){
//        return diag;
//    }
}
