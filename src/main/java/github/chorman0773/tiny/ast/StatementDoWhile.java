package github.chorman0773.tiny.ast;

public class StatementDoWhile  extends Statement {

    private final BooleanExpr ctrl;
    private final Statement looped;

    public StatementDoWhile(BooleanExpr ctrl, Statement looped){
        this.ctrl = ctrl;
        this.looped = looped;
    }

    public BooleanExpr getControl(){
        return ctrl;
    }

    public Statement getLooped(){
        return looped;
    }
}
