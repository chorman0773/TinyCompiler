package github.chorman0773.tiny.sema.ssa.stat;

public class StatStoreDead extends SSAStatement {
    private final int localN;

    public StatStoreDead(int localN){
        this.localN = localN;
    }

    public int getLocalNumber(){
        return localN;
    }

    public String toString(){
        return "storage dead _"+localN;
    }

}
