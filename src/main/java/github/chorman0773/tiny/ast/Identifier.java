package github.chorman0773.tiny.ast;

import github.chorman0773.tiny.lex.Span;

public record Identifier(String name, Span span) {
    public String toString(){
        return name + " /**"+span+"**/";
    }
}
