package github.chorman0773.tiny.lex;

public record Span(String fileName, int lineStart, int colStart, int lineEnd, int colEnd) {
    public String toString(){
        return fileName + " @ (" + lineStart + ':' + colStart + "), (" + lineEnd + ':' + colEnd + ")";
    }

}
