package github.chorman0773.tiny.ast;

import java.util.Collections;
import java.util.List;

public class Block {
    private final List<Statement> stats;

    public Block(List<Statement> stats){
        this.stats = stats;
    }

    public List<Statement> getStatements(){
        return Collections.unmodifiableList(stats);
    }

    public String toString(){
        return stats.toString();
    }
}
