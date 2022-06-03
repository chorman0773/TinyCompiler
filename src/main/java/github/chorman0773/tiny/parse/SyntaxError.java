package github.chorman0773.tiny.parse;

public class SyntaxError extends Exception{
    private final Diagnostic diag;
    public SyntaxError(Diagnostic diag) {
        super(diag.text());
        this.diag = diag;
    }

    public Diagnostic getDiagnostic(){
        return diag;
    }
}
