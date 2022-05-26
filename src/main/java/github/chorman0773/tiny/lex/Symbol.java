package github.chorman0773.tiny.lex;

import java.util.Optional;

public class Symbol {
    private final TinySym sym;
    private final Object value;
    private final Span span;

    public Symbol(TinySym sym, Object value, Span span){
        this.sym = sym;
        this.value = value;
        this.span = span;
    }

    public Symbol(TinySym sym, Object value){
        this(sym,value,null);
    }

    public Symbol(TinySym sym){
        this(sym,null);
    }

    public TinySym getSym(){
        return sym;
    }

    public Object getValue(){
        return value;
    }

    public Span getSpan(){
        return this.span;
    }

    @SuppressWarnings("unchecked")
    public <S> Optional<S> checkValue(TinySym kind){
        if(this.sym!=kind)
            return Optional.empty();
        else
            return Optional.of((S)value);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(sym).append(" (").append(value).append(") [").append(span).append("]");
        return builder.toString();
    }
}
