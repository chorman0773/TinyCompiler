package github.chorman0773.tiny.ast;

public class StatementWhile extends Statement {
    private final BooleanExpr ctrl;
    private final Statement looped;

    public StatementWhile(BooleanExpr ctrl, Statement looped){
        this.ctrl = ctrl;
        this.looped = looped;
    }

    public BooleanExpr getControl(){
        return ctrl;
    }

    public Statement getLooped(){
        return looped;
    }

    public boolean isBlock(){
        return true;
    }
}
