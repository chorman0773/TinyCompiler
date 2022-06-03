package github.chorman0773.tiny.lex;

import github.chorman0773.tiny.ExtensionsState;
import github.chorman0773.tiny.util.Peek;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TinyLexer {
    private String filename;
    private Peek<Integer> it;
    private int col;
    private int line;
    private ExtensionsState exts;


    public TinyLexer(String fname,InputStream in,ExtensionsState exts){
        this(fname,new InputStreamReader(in),exts);
    }

    public TinyLexer(String fname,Reader r,ExtensionsState exts){
        it = new Peek<>(new CodePointIterator(new ReadIterator(new BufferedReader(r))));
        this.filename = fname;
        this.exts = exts;
    }

    private Optional<Symbol> lexComment(int lineStart, int colStart){
        int stage = 0;
        while(true) {
            if(!it.hasNext())
                return Optional.of(new Symbol(TinySym.Error,"Unexpected End of File in Comment",new Span(filename,lineStart,colStart,line,col)));
            int c = it.next();
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
                }else if(c=='\r'){
                    if(!it.hasNext())
                        return Optional.of(new Symbol(TinySym.Error,"Unexpected End of File in Comment",new Span(filename,lineStart,colStart,line,col)));
                    c = it.next();
                    if(c!='\n')
                        return Optional.of(new Symbol(TinySym.Error,"Expected Newline after Carriage Return, got " + c, new Span(filename,line,col,line,col+1)));
                    else{
                        line++;
                        col = 0;
                    }
                }
            }
        }
    }

    public Symbol nextToken() {
        int c;
        do {
            var tok = it.optNext();
            if (tok.isEmpty())
                return new Symbol(TinySym.Eof,null,new Span(filename,line,col,line,col));

            c =tok.get();
            if(c=='\n'){
                col = 0;
                line++;
            }if(c=='\r'){
                tok = it.optNext();
                if(tok.isEmpty())
                    return new Symbol(TinySym.Error,"Expected Newline after Carriage Return, got EOF", new Span(filename,line,col,line,col+1));

                c = tok.get();
                if(c!='\n')
                    return new Symbol(TinySym.Error,"Expected Newline after Carriage Return, got " + c, new Span(filename,line,col,line,col+1));
                else{
                    col = 0;
                    line++;
                    continue;
                }
            }else if(c=='/') {
                col++;
                int lineStart = line;
                int colStart = col;
                tok = it.peek();
                if(tok.equals(Optional.of((int)'*'))) {
                    it.next();
                    col++;
                    tok = it.peek();
                    if(tok.equals(Optional.of((int)'*'))) {
                        it.next();
                        col++;
                        var err = lexComment(lineStart, colStart);
                        if(err.isPresent())
                            return err.get();
                        continue;
                    }else
                        return new Symbol(TinySym.Error,"Unexpected /* that did not introduce a comment",new Span(filename,lineStart,colStart,line,col));
                }
            }else if(Character.isWhitespace(c))
                col++;

            if(!Character.isWhitespace(c))
                break;
        }while(true);
        int lineStart = line;
        int colStart = col;
        col++;
        if(exts.isIdentifierStart(c)){
            StringBuilder id = new StringBuilder();
            id.appendCodePoint(c);

            do {
                var tok = it.peek();
                if (tok.isEmpty())
                    break;
                c =tok.get();
                if(exts.isIdentifierPart(c)){
                    col++;
                    id.appendCodePoint(c);
                    it.next();
                }
            }while(exts.isIdentifierPart(c));
            String ident = id.toString();
            Span s = new Span(this.filename,lineStart,colStart,line,col);
            TinySym kind = exts.isKeyword(ident)?TinySym.Keyword:TinySym.Identifier;
            return new Symbol(kind,ident,s);
        }else if(Character.isDigit(c)) {
            StringBuilder num = new StringBuilder();
            num.appendCodePoint(c);
            do {
                var tok = it.peek();
                if(tok.isEmpty())
                    break;
                c = tok.get();
                if(Character.isDigit(c)) {
                    it.next();
                    col++;
                    num.appendCodePoint(c);
                }else if(Character.isUnicodeIdentifierStart(c)) {
                    return new Symbol(TinySym.Error,"Expected a digit, got '"+c+"'",new Span(filename,lineStart,colStart,line,col));
                }else
                    break;
            }while(true);

            var tok = it.peek();
            if(tok.equals(Optional.of((int)'.'))) {
                it.next();
                col++;
                tok = it.optNext();
                if(tok.isEmpty())
                    return new Symbol(TinySym.Error,"Expected a digit, got EOF",new Span(filename,lineStart,colStart,line,col));
                c = tok.get();
                if(!Character.isDigit(c))
                    return new Symbol(TinySym.Error,"Expected a digit, got '"+c+"'",new Span(filename,lineStart,colStart,line,col));
                num.appendCodePoint(c);
                do {
                    tok = it.peek();
                    if(tok.isEmpty())
                        break;
                    c = tok.get();
                    if(Character.isDigit(c)) {
                        it.next();
                        col++;
                        num.appendCodePoint(c);
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
            strLit.appendCodePoint(c);
            do {
                if(!it.hasNext())
                    return new Symbol(TinySym.Error,"Expected \" to terminate string, got EOF",new Span(filename,lineStart,colStart,line,col));
                c = it.next();
                col++;
                if(c=='\n')
                    return new Symbol(TinySym.Error, "Unexpected newline in string literal", new Span(filename, line, col-1, line+1, 0));
                else if(c=='\r')
                    return new Symbol(TinySym.Error, "Unexpected Carriage Return in string literal", new Span(filename, line, col-1, line+1, 0));
                strLit.appendCodePoint(c);
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
                case '/', '*', '+', '-', ',', ';' -> {return new Symbol(TinySym.Sigil,Character.toString(c),new Span(filename,lineStart,colStart,line,col));}
                case ':','!' -> {
                    String tok = Character.toString(c);
                    if(!it.hasNext())
                        return new Symbol(TinySym.Error,"Expected =, got EOF",new Span(filename,lineStart,colStart,line,col));
                    c = it.next();
                    col++;
                    if(c!='=')
                        return new Symbol(TinySym.Error, "Expected =, got "+Character.toString(c),new Span(filename,lineStart,colStart,line,col));
                    tok += Character.toString(c);
                    return new Symbol(TinySym.Sigil,tok,new Span(filename,lineStart,colStart,line,col));
                }
                case '=','<','>' -> {
                    String tok = Character.toString(c);
                    var nc = it.peek();

                    if(nc.equals(Optional.of((int)'='))){
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

