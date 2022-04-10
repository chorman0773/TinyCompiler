package github.chorman0773.tiny.ast;

public enum Type {
    Int,
    Real,
    String;

    @Override
    public java.lang.String toString() {
        return this.name();
    }
}
