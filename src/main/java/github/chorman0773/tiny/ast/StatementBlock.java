package github.chorman0773.tiny.ast;

public class StatementBlock extends Statement {
    private final Block block;

    public StatementBlock(Block block){
        this.block = block;
    }

    public Block getBlock(){
        return this.block;
    }

    public boolean isBlock(){
        return true;
    }

    public String toString(){
        return block.toString();
    }
}
