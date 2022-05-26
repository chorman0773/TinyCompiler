package github.chorman0773.tiny.sema.ssa.stat;

public class StatUnreachable extends SSAStatement {

    public String toString(){
        return "unreachable";
    }

    @Override
    public boolean isTerminator() {
        return true;
    }
}
