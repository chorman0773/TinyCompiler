package github.chorman0773.tiny.parse;

import github.chorman0773.tiny.ast.*;
import github.chorman0773.tiny.lex.Symbol;
import github.chorman0773.tiny.lex.TinySym;
import github.chorman0773.tiny.util.Peek;

import java.util.*;


public class ProgramParser {

    public static Program parseProgram(Peek<Symbol> it) throws SyntaxError{
        List<MethodDeclaration> decls = new ArrayList<>();
        while(it.hasNext())
            decls.add(parseMethodDeclaration(it));
        return new Program(decls);
    }

    public static MethodDeclaration parseMethodDeclaration(Peek<Symbol> it) throws SyntaxError{
        Type ty = parseType(it);

        Symbol sym = it.peek().orElseThrow(()->new SyntaxError("Unexpected End of File"));

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
            Type paramTy = parseType(peek);
            String paramName = getNextToken(peek,TinySym.Identifier);
            parameters.add(new Parameter(paramTy,paramName));
            if(!peek.hasNext())
                break;
            checkNextToken(it,TinySym.Sigil,",");
            if(!peek.hasNext())
                throw new SyntaxError("Unexpected end of input");
        }

        Block block = parseBlock(it);

        return new MethodDeclaration(isMain,ty,name,parameters,block);
    }

    public static Block parseBlock(Peek<Symbol> it) throws SyntaxError{
        checkNextToken(it,TinySym.Keyword,"BEGIN");
        List<Statement> stats = new ArrayList<>();
        while(true){
            Symbol sym = it.peek().orElseThrow(() -> new SyntaxError("Unexpected EOF"));

            if(sym.<String>checkValue(TinySym.Keyword).equals(Optional.of("END"))) {
                it.next();
                break;
            }
            else
                stats.add(parseStatement(it));
        }
        return new Block(stats);
    }

    public static <S> S getNextToken(Peek<Symbol> it, TinySym kind) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError("Unexpected EOF");
        else{
            Symbol sym = it.next();
            return sym.<S>checkValue(kind).orElseThrow(()->new SyntaxError("Unexpected token, got "+sym+", expected a "+kind));
        }
    }

    public static <S> void checkNextToken(Peek<Symbol> it,TinySym kind,S value) throws SyntaxError {
        if(!it.hasNext())
            throw new SyntaxError("Unexpected EOF");
        else{
            Symbol sym = it.next();
            if(!sym.<S>checkValue(kind).equals(Optional.of(value)))
                throw new SyntaxError("Unexpected token, got "+sym+", expected "+kind+" ("+value + ")");
        }
    }

    public static Statement parseStatement(Peek<Symbol> it) throws SyntaxError{
        Symbol sym = it.peek().orElseThrow(() -> new SyntaxError("Unexpected EOF"));

        Optional<String> kw = sym.checkValue(TinySym.Keyword);
        Statement stat;
        if(kw.isPresent()){
            stat = switch(kw.get()){
                case "IF" -> {
                    it.next();
                    List<Symbol> syms = it.optNext().flatMap(s->s.<List<Symbol>>checkValue(TinySym.ParenGroup)).orElseThrow(()->new SyntaxError("Unexpected End of File"));
                    Peek<Symbol> controlPeek = new Peek<>(syms.iterator());
                    BooleanExpr control = parseBooleanExpr(controlPeek);
                    if(controlPeek.hasNext())
                        throw new SyntaxError("Unexpected leftover tokens " + controlPeek.next());
                    Statement then = parseStatement(it);
                    Optional<String> next = it.peek().flatMap(s->s.checkValue(TinySym.Keyword));
                    Statement orelse = null;
                    if(next.equals(Optional.of("ELSE"))){
                        it.next();
                        orelse = parseStatement(it);
                    }
                    yield new StatementIf(control,then,orelse);
                }
                case "RETURN" -> {
                    it.next();
                    yield new StatementReturn(parseExpr(it));
                }
                case "BEGIN" -> new StatementBlock(parseBlock(it));
                case "WRITE" -> {
                    it.next();
                    Peek<Symbol> syms = it.optNext().flatMap(s->s.<List<Symbol>>checkValue(TinySym.ParenGroup)).map(List::iterator).map(Peek::new).orElseThrow(() -> new SyntaxError("Unexpected Token"));

                    Expression expr = parseExpr(syms);

                    checkNextToken(syms,TinySym.Sigil,",");

                    String qpath = syms.optNext().flatMap(s->s.<String>checkValue(TinySym.String)).orElseThrow(() -> new SyntaxError("Unexpected Token"));
                    String path = qpath.substring(1,qpath.length()-1);
                    yield new StatementWrite(expr,path);
                }
                case "READ" -> {
                    it.next();
                    Peek<Symbol> syms = it.optNext().flatMap(s->s.<List<Symbol>>checkValue(TinySym.ParenGroup)).map(List::iterator).map(Peek::new).orElseThrow(() -> new SyntaxError("Unexpected Token"));

                    String id = syms.optNext().flatMap(s->s.<String>checkValue(TinySym.Identifier)).orElseThrow(() -> new SyntaxError("Unexpected Token"));

                    checkNextToken(syms,TinySym.Sigil,",");

                    String qpath = syms.optNext().flatMap(s->s.<String>checkValue(TinySym.String)).orElseThrow(() -> new SyntaxError("Unexpected Token"));
                    String path = qpath.substring(1,qpath.length()-1);
                    yield new StatementRead(id,path);
                }
                default -> {
                    Type ty = parseType(it);
                    String id = it.optNext().flatMap(s->s.<String>checkValue(TinySym.Identifier)).orElseThrow(() -> new SyntaxError("Unexpected Token"));
                    Expression init = null;
                    if(it.peek().flatMap(s->s.<String>checkValue(TinySym.Sigil)).equals(Optional.of(":="))){
                        it.next();
                        init = parseExpr(it);
                    }
                    yield new StatementDeclaration(ty,id,init);
                }
            };
        }else{
            String id = it.optNext().flatMap(s->s.<String>checkValue(TinySym.Identifier)).orElseThrow(() -> new SyntaxError("Unexpected Token"));
            checkNextToken(it,TinySym.Sigil,":=");
            Expression init = parseExpr(it);
            stat = new StatementAssignment(id,init);
        }

        if(!stat.isBlock()){
            checkNextToken(it,TinySym.Sigil,";");
        }
        return stat;
    }

    public static BooleanExpr parseBooleanExpr(Peek<Symbol> it) throws SyntaxError {
        Expression left = parseExpr(it);
        BooleanOp op = switch(it.optNext().flatMap(sym->sym.<String>checkValue(TinySym.Sigil)).orElseThrow(() -> new SyntaxError("Unexpected Token"))){
            case "==" -> BooleanOp.CmpEq;
            case "!=" -> BooleanOp.CmpNe;
            default -> {throw new SyntaxError("Unexpected Token");}
        };
        Expression right = parseExpr(it);

        return new BooleanExpr(op,left,right);
    }

    public static Expression parseExpr(Peek<Symbol> it) throws SyntaxError{
        return parseBinaryExpr(it,0);
    }

    private static final Map<String,OperatorTuple> OPERATORS = new HashMap<>(){{
        put("+",new OperatorTuple(BinaryOp.Add,1,2));
        put("-",new OperatorTuple(BinaryOp.Sub,1,2));
        put("*",new OperatorTuple(BinaryOp.Mul, 3, 4));
        put("/",new OperatorTuple(BinaryOp.Div, 3, 4));
    }};

    public static Expression parseBinaryExpr(Peek<Symbol> it, int precedence) throws SyntaxError{
        Expression left = parseSimpleExpr(it);
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
            Expression right = parseBinaryExpr(it,op.rbp());
            left = new ExpressionBinary(op.op(),left,right);
        }

        return left;
    }

    @SuppressWarnings("unchecked")
    public static Expression parseSimpleExpr(Peek<Symbol> it) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError("Unexpected End of File");
        Symbol sym = it.next();

        return switch(sym.getSym()){
            case Identifier -> {
                String id = (String)sym.getValue();
                Optional<List<Symbol>> optParams = it.peek().flatMap(s->s.checkValue(TinySym.ParenGroup));
                if(optParams.isPresent()){
                    List<Symbol> paramToks = optParams.get();
                    it.next();
                    List<Expression> args = new ArrayList<>();
                    Peek<Symbol> inner = new Peek<>(paramToks.iterator());
                    while(inner.hasNext()){
                        args.add(parseExpr(inner));
                        if(!inner.hasNext())
                            break;
                        Symbol comma = inner.next();
                        if(!comma.checkValue(TinySym.Sigil).equals(Optional.of(",")))
                            throw new SyntaxError("Unexpected token "+ comma);
                        if(!inner.hasNext())
                            throw new SyntaxError("Unexpected End of Input");
                    }
                    yield new ExpressionCall(id,args);
                }else
                    yield new ExpressionId(id);
            }
            case ParenGroup ->  {
                Peek<Symbol> inner = new Peek<>(((List<Symbol>)sym.getValue()).iterator());

                Expression expr = parseExpr(inner);
                if(inner.hasNext())
                    throw new SyntaxError("Unexpected leftover tokens " + inner.next());
                yield new ParenExpr(expr);
            }
            case Number -> new ExpressionNumber((Double)sym.getValue());
            default -> throw new SyntaxError("Expected expression got "+sym);
        };
    }

    public static Type parseType(Peek<Symbol> it) throws SyntaxError{
        if(!it.hasNext())
            throw new SyntaxError("Unexpected End of File");
        Symbol ty = it.next();

        Optional<String> keyword = ty.checkValue(TinySym.Keyword);


        return switch(keyword.orElseThrow(()->new SyntaxError("Expected keyword, got "+ty))){
            case "INT" -> Type.Int;
            case "REAL" -> Type.Real;
            case "STRING" -> Type.String;
            default -> throw new SyntaxError("Expected INT, REAL, or STRING, got "+keyword.get());
        };
    }
}
