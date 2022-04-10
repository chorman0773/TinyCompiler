package github.chorman0773.tiny.ast;

import java.util.Optional;

public class StatementIf extends Statement {
    private final BooleanExpr ctrl;
    private final Statement then;
    private final Statement orelse;

    public StatementIf(BooleanExpr control, Statement then, Statement orelse){
        this.ctrl = control;
        this.then = then;
        this.orelse = orelse;
    }

    public BooleanExpr getControl(){
        return ctrl;
    }

    public Statement getIf(){
        return then;
    }

    public Optional<Statement> getElse(){
        return Optional.ofNullable(orelse);
    }

    public boolean isBlock(){
        return true;
    }

    public String toString(){
        StringBuilder build = new StringBuilder();
        build.append("IF(")
                .append(ctrl)
                .append(") ")
                .append(then);
        if(orelse!=null)
            build.append(" ELSE ").append(orelse);
        return build.toString();
    }
}
