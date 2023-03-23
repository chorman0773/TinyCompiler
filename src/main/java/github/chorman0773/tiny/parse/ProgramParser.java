package github.chorman0773.tiny.parse;

import github.chorman0773.tiny.ExtensionsState;
import github.chorman0773.tiny.ast.*;
import github.chorman0773.tiny.lex.Span;
import github.chorman0773.tiny.lex.Symbol;
import github.chorman0773.tiny.lex.TinySym;
import github.chorman0773.tiny.util.Peek;

import java.util.*;


public class ProgramParser {

    public static Program parseProgram(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{

        List<TopLevelDeclaration> decls = new ArrayList<>();
        while(it.hasNext())
            decls.add(parseDeclaration(it, exts));
        return new Program(decls);
    }

    public static TopLevelDeclaration parseDeclaration(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        if(it.peek().flatMap(s->s.<String>checkValue(TinySym.Keyword)).equals(Optional.of("IMPORT"))){
            it.next();
            Symbol sym = it.peek().orElseThrow(()->new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty())));

            String name = getNextToken(it,TinySym.Identifier);

            checkNextToken(it,TinySym.Sigil,";");

            return new ImportDecl(name);
        }else{
            return parseMethodDeclaration(it,exts);
        }
    }

    public static MethodDeclaration parseMethodDeclaration(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        Type ty = parseType(it,exts);

        Symbol sym = it.peek().orElseThrow(()->new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty())));

        Optional<String> main = sym.checkValue(TinySym.Keyword);

        boolean isMain = false;
        if(main.isPresent()){
            checkNextToken(it,TinySym.Keyword,"MAIN");
            isMain = true;
        }

        String name = getNextToken(it,TinySym.Identifier);

        List<Symbol> paramSyms = getNextToken(it,TinySym.ParenGroup);

        Peek<Symbol> peek = new Peek<>(paramSyms.iterator());

        List<Parameter> parameters = new ArrayList<>();
        while(peek.hasNext()){
            Type paramTy = parseType(peek, exts);
            String paramName = getNextToken(peek,TinySym.Identifier);
            parameters.add(new Parameter(paramTy,paramName));
            if(!peek.hasNext())
                break;
            checkNextToken(peek,TinySym.Sigil,",");
            if(!peek.hasNext())
                throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));
        }

        Block block = parseBlock(it,exts);

        return new MethodDeclaration(isMain,ty,name,parameters,block);
    }

    public static Block parseBlock(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        checkNextToken(it,TinySym.Keyword,"BEGIN");
        List<Statement> stats = new ArrayList<>();
        while(true){
            Symbol sym = it.peek().orElseThrow(() -> new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty())));

            if(sym.<String>checkValue(TinySym.Keyword).equals(Optional.of("END"))) {
                it.next();
                break;
            }
            else
                stats.add(parseStatement(it,exts));
        }
        return new Block(stats);
    }

    public static Identifier getIdentifierToken(Peek<Symbol> it) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));
        else{
            Symbol sym = it.next();
            if(sym.getSym()==TinySym.Keyword)
                throw new SyntaxError(new Diagnostic("Unexpected token. Expected an Identifier, got "+sym,sym.getSpan(),ExtensionsState.keywordExtension((String)sym.getValue())));
            else if(sym.getSym()!=TinySym.Identifier)
                throw new SyntaxError(new Diagnostic("Unexpected token. Expected an Identifier, got"+sym,sym.getSpan(),Optional.empty()));
            else
                return new Identifier((String)sym.getValue(),sym.getSpan());
        }
    }

    public static <S> S getNextToken(Peek<Symbol> it, TinySym kind) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));
        else{
            Symbol sym = it.next();
            if(sym.getSym()==TinySym.Keyword&&kind==TinySym.Identifier)
                throw new SyntaxError(new Diagnostic("Unexpected token. Expected an Identifier, got "+sym,sym.getSpan(),ExtensionsState.keywordExtension((String)sym.getValue())));
            return sym.<S>checkValue(kind).orElseThrow(()->new SyntaxError(new Diagnostic("Unexpected token, got "+sym+", expected a "+kind,sym.getSpan(),Optional.empty())));
        }
    }

    public static Peek<Symbol> getNextGroup(Peek<Symbol> it) throws SyntaxError{
        List<Symbol> group = getNextToken(it,TinySym.ParenGroup);
        return new Peek<>(group.iterator());
    }

    public static <S> void checkNextToken(Peek<Symbol> it,TinySym kind,S value) throws SyntaxError {
        if(!it.hasNext())
            throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));
        else{
            Symbol sym = it.next();
            if(sym.getSym()==TinySym.Keyword&&kind==TinySym.Identifier)
                throw new SyntaxError(new Diagnostic("Unexpected token. Expected an Identifier, got "+sym,sym.getSpan(),ExtensionsState.keywordExtension((String)sym.getValue())));
            if(!sym.<S>checkValue(kind).equals(Optional.of(value)))
                throw new SyntaxError(new Diagnostic("Unexpected token, got "+sym+", expected a "+kind,sym.getSpan(),Optional.empty()));
        }
    }

    public static Statement parseStatement(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        Symbol sym = it.peek().orElseThrow(() ->new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty())));

        Optional<String> kw = sym.checkValue(TinySym.Keyword);
        Statement stat;
        if(kw.isPresent()){
            stat = switch(kw.get()){
                case "IF" -> {
                    it.next();
                    List<Symbol> syms = it.optNext().flatMap(s->s.<List<Symbol>>checkValue(TinySym.ParenGroup)).orElseThrow(()->new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty())));
                    Peek<Symbol> controlPeek = new Peek<>(syms.iterator());
                    BooleanExpr control = parseBooleanExpr(controlPeek, exts);
                    if(controlPeek.hasNext()){
                        Symbol next = controlPeek.next();
                        throw new SyntaxError(new Diagnostic("Unexpected leftover tokens in parenthesis group",next.getSpan(),Optional.empty()));
                    }
                    Statement then = parseStatement(it,exts);
                    Optional<String> next = it.peek().flatMap(s->s.checkValue(TinySym.Keyword));
                    Statement orelse = null;
                    if(next.equals(Optional.of("ELSE"))){
                        it.next();
                        orelse = parseStatement(it,exts);
                    }
                    yield new StatementIf(control,then,orelse);
                }
                case "RETURN" -> {
                    it.next();
                    yield new StatementReturn(parseExpr(it,exts));
                }
                case "BEGIN" -> new StatementBlock(parseBlock(it,exts));
                case "WRITE" -> {
                    it.next();
                    Peek<Symbol> syms = getNextGroup(it);

                    Expression expr = parseExpr(syms,exts);

                    checkNextToken(syms,TinySym.Sigil,",");

                    String qpath = getNextToken(syms,TinySym.String);
                    String path = qpath.substring(1,qpath.length()-1);
                    yield new StatementWrite(expr,path);
                }
                case "READ" -> {
                    it.next();
                    Peek<Symbol> syms = getNextGroup(it);

                    String id = getNextToken(syms,TinySym.Identifier);

                    checkNextToken(syms,TinySym.Sigil,",");

                    String qpath = getNextToken(syms,TinySym.String);
                    String path = qpath.substring(1,qpath.length()-1);
                    yield new StatementRead(id,path);
                }
                case "WHILE" -> {
                    it.next();
                    assert exts.hasExtension(ExtensionsState.Extension.While) : "ICE: Got WHILE keyword but While extension is disabled";

                    var syms = getNextGroup(it);

                    BooleanExpr expr = parseBooleanExpr(syms,exts);

                    if(syms.hasNext()){
                        Symbol next = syms.next();
                        throw new SyntaxError(new Diagnostic("Unexpected leftover tokens in parenthesis group",next.getSpan(),Optional.empty()));
                    }

                    Statement next = parseStatement(it,exts);
                    yield new StatementWhile(expr,next);
                }
                case "DO" -> {
                    it.next();
                    assert exts.hasExtension(ExtensionsState.Extension.While) : "ICE: Got WHILE keyword but While extension is disabled";

                    Statement next = parseStatement(it,exts);

                    checkNextToken(it,TinySym.Keyword,"WHILE");

                    var syms = getNextGroup(it);

                    BooleanExpr expr = parseBooleanExpr(syms,exts);

                    if(syms.hasNext()){
                        Symbol tok = syms.next();
                        throw new SyntaxError(new Diagnostic("Unexpected leftover tokens in parenthesis group",tok.getSpan(),Optional.empty()));
                    }

                    yield new StatementDoWhile(expr,next);
                }
                default -> {
                    Type ty = parseType(it, exts);
                    Identifier name = getIdentifierToken(it);
                    Expression init = null;
                    if(it.peek().flatMap(s->s.<String>checkValue(TinySym.Sigil)).equals(Optional.of(":="))){
                        it.next();
                        init = parseExpr(it,exts);
                    }
                    yield new StatementDeclaration(ty,name,init);
                }
            };
        }else{
            Identifier name = getIdentifierToken(it);
            checkNextToken(it,TinySym.Sigil,":=");
            Expression init = parseExpr(it,exts);
            stat = new StatementAssignment(name,init);
        }

        if(!stat.isBlock()){
            checkNextToken(it,TinySym.Sigil,";");
        }
        return stat;
    }

    public static BooleanExpr parseBooleanExpr(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError {
        Expression left = parseExpr(it,exts);
        Optional<Span> span = it.peek().map(Symbol::getSpan);
        String sig = getNextToken(it,TinySym.Sigil);
        CompareOp op = switch(sig){
            case "==" -> CompareOp.CmpEq;
            case "!=" -> CompareOp.CmpNe;
            case "<" -> {
                if(!exts.hasExtension(ExtensionsState.Extension.CmpRel))
                    throw new SyntaxError(new Diagnostic("Unexpected token "+sig,span.get(),Optional.of(ExtensionsState.Extension.CmpRel)));
                yield CompareOp.CmpLt;
            }
            case ">" -> {
                if(!exts.hasExtension(ExtensionsState.Extension.CmpRel))
                    throw new SyntaxError(new Diagnostic("Unexpected token "+sig,span.get(),Optional.of(ExtensionsState.Extension.CmpRel)));
                yield CompareOp.CmpGt;
            }
            case "<=" -> {
                if(!exts.hasExtension(ExtensionsState.Extension.CmpRel))
                    throw new SyntaxError(new Diagnostic("Unexpected token "+sig,span.get(),Optional.of(ExtensionsState.Extension.CmpRel)));
                yield CompareOp.CmpLe;
            }
            case ">=" -> {
                if(!exts.hasExtension(ExtensionsState.Extension.CmpRel))
                    throw new SyntaxError(new Diagnostic("Unexpected token "+sig,span.get(),Optional.of(ExtensionsState.Extension.CmpRel)));
                yield CompareOp.CmpGe;
            }
            default -> {throw new SyntaxError(new Diagnostic("Unexpected tokoken "+sig,span.get(),Optional.empty()));}
        };
        Expression right = parseExpr(it,exts);

        return new BooleanExpr(op,left,right);
    }

    public static Expression parseExpr(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        return parseBinaryExpr(it,0,exts);
    }

    private static final Map<String,OperatorTuple> OPERATORS = new HashMap<>(){{
        put("+",new OperatorTuple(BinaryOp.Add,1,2));
        put("-",new OperatorTuple(BinaryOp.Sub,1,2));
        put("*",new OperatorTuple(BinaryOp.Mul, 3, 4));
        put("/",new OperatorTuple(BinaryOp.Div, 3, 4));
    }};

    public static Expression parseBinaryExpr(Peek<Symbol> it, int precedence, ExtensionsState exts) throws SyntaxError{
        Expression left = parseSimpleExpr(it,exts);
        while(it.hasNext()){
            Symbol peeked = it.peek().orElseThrow();
            Optional<OperatorTuple> peekedOp = peeked.<String>checkValue(TinySym.Sigil)
                    .flatMap(sym->Optional.ofNullable(OPERATORS.get(sym)));
            if(peekedOp.isEmpty())
                break;

            OperatorTuple op = peekedOp.get();

            if(op.lbp()<precedence)
                break;
            it.next();
            Expression right = parseBinaryExpr(it,op.rbp(),exts);
            left = new ExpressionBinary(op.op(),left,right);
        }

        return left;
    }

    @SuppressWarnings("unchecked")
    public static Expression parseSimpleExpr(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));
        Symbol sym = it.next();

        return switch(sym.getSym()){
            case Identifier -> {
                String name = (String)sym.getValue();
                Span span = sym.getSpan();
                Identifier id = new Identifier(name,span);
                Optional<List<Symbol>> optParams = it.peek().flatMap(s->s.checkValue(TinySym.ParenGroup));
                if(optParams.isPresent()){
                    List<Symbol> paramToks = optParams.get();
                    it.next();
                    List<Expression> args = new ArrayList<>();
                    Peek<Symbol> inner = new Peek<>(paramToks.iterator());
                    while(inner.hasNext()){
                        args.add(parseExpr(inner,exts));
                        if(!inner.hasNext())
                            break;
                        checkNextToken(it,TinySym.Sigil,",");
                        if(!inner.hasNext())
                            throw new SyntaxError(new Diagnostic("Unexpected End of Input",null,Optional.empty()));
                    }
                    yield new ExpressionCall(id,args);
                }else
                    yield new ExpressionId(id);
            }
            case ParenGroup ->  {
                Peek<Symbol> inner = new Peek<>(((List<Symbol>)sym.getValue()).iterator());

                Expression expr = parseExpr(inner,exts);
                if(inner.hasNext())
                    throw new SyntaxError(new Diagnostic("Unexpected leftover tokens in parenthesis group",inner.next().getSpan(),Optional.empty()));
                yield new ParenExpr(expr);
            }
            case Number -> new ExpressionNumber((Double)sym.getValue());
            default -> throw new SyntaxError(new Diagnostic("Expected expression got "+sym,sym.getSpan(),Optional.empty()));
        };
    }

    public static Type parseType(Peek<Symbol> it, ExtensionsState exts) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError(new Diagnostic("Unexpected EOF",null,Optional.empty()));

        Optional<Span> span = it.peek().map(Symbol::getSpan);
        String keyword = getNextToken(it,TinySym.Keyword);
        return switch(keyword){
            case "INT" -> Type.Int;
            case "REAL" -> Type.Real;
            case "STRING" -> Type.String;
            default -> throw new SyntaxError(new Diagnostic("Expected INT, REAL, or STRING, got "+keyword,span.get(),Optional.empty()));
        };
    }
}
