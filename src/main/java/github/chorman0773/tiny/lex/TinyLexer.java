package github.chorman0773.tiny.lex;

import github.chorman0773.tiny.util.Peek;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TinyLexer {
    private String filename;
    private Peek<Character> it;
    private int col;
    private int line;


    public TinyLexer(String fname,InputStream in){
        this(fname,new InputStreamReader(in));
    }

    public TinyLexer(String fname,Reader r){
        it = new Peek<>(new ReadIterator(new BufferedReader(r)));
        this.filename = fname;
    }

    private Optional<Symbol> lexComment(int lineStart, int colStart){
        int stage = 0;
        while(true) {
            if(!it.hasNext())
                return Optional.of(new Symbol(TinySym.Error,"Unexpected End of File in Comment",new Span(filename,lineStart,colStart,line,col)));
            char c = it.next();
            col++;
            if(c=='*'&&(stage==0||stage==1))
                stage++;
            else if(c=='/'&&stage==2)
                return Optional.empty();
            else {
                stage = 0;
                if(c=='\n') {
                    line++;
                    col = 0;
                }
            }
        }
    }

    public Symbol nextToken() {
        char c;
        do {
            var tok = it.optNext();
            if (tok.isEmpty())
                return new Symbol(TinySym.Eof);

            c =tok.get();
            if(c=='\n'){
                col = 0;
                line++;
            }else if(c=='/') {
                col++;
                int lineStart = line;
                int colStart = col;
                tok = it.peek();
                if(tok.equals(Optional.of('*'))) {
                    System.err.println("Starting Comment Group");
                    it.next();
                    col++;
                    tok = it.peek();
                    if(tok.equals(Optional.of('*'))) {
                        it.next();
                        col++;
                        var err = lexComment(lineStart, colStart);
                        if(err.isPresent())
                            return err.get();
                    }else
                        return new Symbol(TinySym.Error,"Unexpected /* that did not introduce a comment",new Span(filename,lineStart,colStart,line,col));
                }
                continue;
            }else if(Character.isWhitespace(c))
                col++;

            if(!Character.isWhitespace(c))
                break;
        }while(true);
        int lineStart = line;
        int colStart = col;
        col++;
        if(Character.isUnicodeIdentifierStart(c)){
            StringBuilder id = new StringBuilder();
            id.append(c);

            do {
                var tok = it.peek();
                if (tok.isEmpty())
                    break;
                c =tok.get();
                if(Character.isUnicodeIdentifierPart(c)){
                    col++;
                    id.append(c);
                    it.next();
                }
            }while(Character.isUnicodeIdentifierPart(c));
            String ident = id.toString();
            Span s = new Span(this.filename,lineStart,colStart,line,col);
            TinySym kind = switch(ident){
                case "INT", "REAL", "STRING", "MAIN", "READ", "WRITE", "BEGIN", "END", "IF", "ELSE", "RETURN" -> TinySym.Keyword;
                default -> TinySym.Identifier;
            };

            return new Symbol(kind,ident,s);
        }else if(Character.isDigit(c)) {
            StringBuilder num = new StringBuilder();
            num.append(c);
            do {
                var tok = it.peek();
                if(tok.isEmpty())
                    break;
                c = tok.get();
                if(Character.isDigit(c)) {
                    it.next();
                    col++;
                    num.append(c);
                }else if(Character.isUnicodeIdentifierStart(c)) {
                    return new Symbol(TinySym.Error,"Expected a digit, got '"+c+"'",new Span(filename,lineStart,colStart,line,col));
                }else
                    break;
            }while(true);

            var tok = it.peek();
            if(tok.equals(Optional.of('.'))) {
                it.next();
                col++;
                tok = it.optNext();
                if(tok.isEmpty())
                    return new Symbol(TinySym.Error,"Expected a digit, got EOF",new Span(filename,lineStart,colStart,line,col));
                c = tok.get();
                if(!Character.isDigit(c))
                    return new Symbol(TinySym.Error,"Expected a digit, got '"+c+"'",new Span(filename,lineStart,colStart,line,col));
                num.append(c);
                do {
                    tok = it.peek();
                    if(tok.isEmpty())
                        break;
                    c = tok.get();
                    if(Character.isDigit(c)) {
                        it.next();
                        col++;
                        num.append(c);
                    }else if(Character.isUnicodeIdentifierStart(c)) {
                        return new Symbol(TinySym.Error,"Expected a digit, got '"+c+"'",new Span(filename,lineStart,colStart,line,col));
                    }else
                        break;
                }while(true);
            }
            String val = num.toString();
            return new Symbol(TinySym.Number,Double.valueOf(val),new Span(filename,lineStart,colStart,line,col));
        }else if(c=='"') {
            StringBuilder strLit = new StringBuilder();
            strLit.append(c);
            do {
                if(!it.hasNext())
                    return new Symbol(TinySym.Error,"Expected \" to terminate string, got EOF",new Span(filename,lineStart,colStart,line,col));
                c = it.next();
                strLit.append(c);
            }while(c!='"');
            return new Symbol(TinySym.String,strLit.toString(),new Span(filename,lineStart,colStart,line,col));
        }else if(c==')'){
            return new Symbol(TinySym.EndGroup,")",new Span(filename,lineStart,colStart,line,col));
        }else if(c=='('){
            List<Symbol> toks = new ArrayList<>();
            Span parenSpan = new Span(filename,lineStart,colStart,line,col);
            do{
                Symbol sym = nextToken();
                if(sym.getSym()==TinySym.Eof)
                    return new Symbol(TinySym.Error,"Expected ), got EOF", new Span(filename,line,col,line,col));
                else if(sym.getSym()==TinySym.Error)
                    return sym;
                else if(sym.getSym()==TinySym.EndGroup)
                    break;
                else
                    toks.add(sym);
            }while(true);
            return new Symbol(TinySym.ParenGroup,toks,parenSpan);
        }else{
            switch(c){
                case '/', '*', '+', '-', ',', ';' -> {return new Symbol(TinySym.Sigil,String.valueOf(c),new Span(filename,lineStart,colStart,line,col));}
                case ':','!' -> {
                    String tok = String.valueOf(c);
                    if(!it.hasNext())
                        return new Symbol(TinySym.Error,"Expected =, got EOF",new Span(filename,lineStart,colStart,line,col));
                    c = it.next();
                    col++;
                    if(c!='=')
                        return new Symbol(TinySym.Error, "Expected =, got "+c,new Span(filename,lineStart,colStart,line,col));
                    tok += c;
                    return new Symbol(TinySym.Sigil,tok,new Span(filename,lineStart,colStart,line,col));
                }
                case '=' -> {
                    String tok = String.valueOf(c);
                    var nc = it.peek();

                    if(nc.equals(Optional.of('='))){
                        it.next();
                        col++;
                        tok += '=';
                    }
                    return new Symbol(TinySym.Sigil,tok,new Span(filename,lineStart,colStart,line,col));
                }
                default -> {return new Symbol(TinySym.Error, "Unexpected character " + c, new Span(filename,lineStart,colStart,line,col));}
            }
        }
    }
}
