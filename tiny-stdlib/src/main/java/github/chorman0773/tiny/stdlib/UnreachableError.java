package github.chorman0773.tiny.stdlib;

class UnreachableError extends Error{
    public UnreachableError(){
        super("Internal Error: Entered Unreachable Code");
    }
}
