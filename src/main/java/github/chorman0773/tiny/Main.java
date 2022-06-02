package github.chorman0773.tiny;

import github.chorman0773.tiny.ast.MethodDeclaration;
import github.chorman0773.tiny.ast.Parameter;
import github.chorman0773.tiny.ast.Program;
import github.chorman0773.tiny.ast.Statement;
import github.chorman0773.tiny.codegen.Codegen;
import github.chorman0773.tiny.codegen.CodegenService;
import github.chorman0773.tiny.codegen.java.JavaCodegenService;
import github.chorman0773.tiny.lex.Symbol;
import github.chorman0773.tiny.lex.TinyLexer;
import github.chorman0773.tiny.lex.TinySym;
import github.chorman0773.tiny.opt.Optimizer;
import github.chorman0773.tiny.parse.ProgramParser;
import github.chorman0773.tiny.parse.SyntaxError;
import github.chorman0773.tiny.sema.ssa.SSAConverter;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.util.Functional;
import github.chorman0773.tiny.util.Peek;

import java.io.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Main {
    private static final String USAGE =
    """
    Usage: tiny-compiler [options] input-file
    Compiles a Tiny File.
    
    Options:
    Arguments mandatory for long options are mandatory for short options as well
        -o, --output-file=<output>: Sets the output file
        --dump=ast|mir: writes the AST or MIR to the output file, instead of running codegen
        --run-opts=<opts>: Specifies which optimizers to run (opts is separated by commas). No optimizations are run by default when generating MIR.
        --codegen=<codegen>: Specifies which codegen to run.
        --codegen-version=<version>: Specifies the output version to use with the specified codegen
        --help, -h: Prints this message and exits
        --version, -V: Prints version information and exits
        --: Ends the list of options
    """;

    public static enum CompilationStage{
        Ast,
        Mir,
        Codegen
    }

    public static void main(String[] args) throws IOException {
        String file = null;
        Iterator<String> iter = Arrays.stream(args).iterator();
        Predicate<String> optFilter = null;
        String desiredCodegen = null;
        String outputVersion = null;
        String outputFile = null;
        CompilationStage stage = CompilationStage.Codegen;
        EnumSet<ExtensionsState.Extension> exts = ExtensionsState.defaultExtensions();
        while(iter.hasNext()){
            String arg = iter.next();
            if(arg.startsWith("--")){
                if(arg.equals("--")){
                    if(iter.hasNext())
                        file = iter.next();
                    break;
                }else if(arg.equals("--run-opts")){
                    if(!iter.hasNext()) {
                        System.err.println("--run-opts needs an argument");
                        System.exit(1);
                    }
                    String[] opts = iter.next().split(",");
                    Arrays.sort(opts);
                    optFilter = Functional.or(optFilter,s->Arrays.binarySearch(opts,s)>=0||Arrays.binarySearch(opts,"all")>=0);
                }else if(arg.startsWith("--run-opts=")){
                    String rest = arg.substring(11);
                    String[] opts = rest.split(",");
                    optFilter = Functional.or(optFilter,s->Arrays.binarySearch(opts,s)>=0||Arrays.binarySearch(opts,"all")>=0);
                }else if(arg.equals("--codegen")){
                    if(!iter.hasNext()) {
                        System.err.println("--codegen needs an argument");
                        System.exit(1);
                    }
                    desiredCodegen = iter.next();
                }else if(arg.startsWith("--codegen=")){
                    desiredCodegen = arg.substring(10);
                }else if(arg.equals("--codegen-version")){
                    if(!iter.hasNext()) {
                        System.err.println("--codegen-version needs an argument");
                        System.exit(1);
                    }
                    outputVersion = iter.next();
                }else if(arg.startsWith("--codegen-version=")){
                    outputVersion = arg.substring(18);
                }else if(arg.equals("--output-file")){
                    if(!iter.hasNext()) {
                        System.err.println("--output-file needs an argument");
                        System.exit(1);
                    }
                    outputFile = iter.next();
                }else if(arg.startsWith("--output-file=")){
                    outputFile = arg.substring(14);
                }else if(arg.equals("--help")){
                    System.out.println(USAGE);
                    System.exit(0);
                }else if(arg.equals("--version")){
                    System.out.println("tiny-compiler 1.0");
                    System.out.println("Copyright (C) 2022, Connor Horman. Release under the terms of the BSD 2-clause with patent license");
                }else if(arg.equals("--dump")){
                    if(!iter.hasNext()) {
                        System.err.println("--dump needs an argument");
                        System.exit(1);
                    }
                    String dump = iter.next();
                    switch(dump){
                        case "ast" -> stage = CompilationStage.Ast;
                        case "mir" -> stage = CompilationStage.Mir;
                        default -> {
                            System.err.println("Unrecognized dump mode " + dump);
                            System.exit(1);
                        }
                    }
                }else if(arg.startsWith("--dump=")){
                    String dump = arg.substring(7);
                    switch(dump){
                        case "ast" -> stage = CompilationStage.Ast;
                        case "mir" -> stage = CompilationStage.Mir;
                        default -> {
                            System.err.println("Unrecognized dump mode " + dump);
                            System.exit(1);
                        }
                    }
                }else if(arg.equals("--extension")){
                    if(!iter.hasNext()) {
                        System.err.println("--extensions needs an argument");
                        System.exit(1);
                    }
                    String rawExts = iter.next();
                    for(String rawExt : rawExts.split(",")){
                        var ext = ExtensionsState.Extension.fromId(rawExt);

                        if(ext.isEmpty()){
                            System.err.println("Unrecognized Extension "+rawExt);
                            System.exit(1);
                        }
                        exts.add(ext.get());
                    }
                }else if(arg.startsWith("--extension=")){
                    String rawExts = arg.substring(12);
                    for(String rawExt : rawExts.split(",")){
                        var ext = ExtensionsState.Extension.fromId(rawExt);

                        if(ext.isEmpty()){
                            System.err.println("Unrecognized Extension "+rawExt);
                            System.exit(1);
                        }
                        exts.add(ext.get());
                    }
                }else if(arg.equals("--no-extension")){
                    if(!iter.hasNext()) {
                        System.err.println("--extensions needs an argument");
                        System.exit(1);
                    }
                    String rawExts = iter.next();
                    for(String rawExt : rawExts.split(",")){
                        var ext = ExtensionsState.Extension.fromId(rawExt);

                        if(ext.isEmpty()){
                            System.err.println("Unrecognized Extension "+rawExt);
                            System.exit(1);
                        }
                        exts.remove(ext.get());
                    }
                }else if(arg.startsWith("--no-extension=")){
                    String rawExts = arg.substring(12);
                    for(String rawExt : rawExts.split(",")){
                        var ext = ExtensionsState.Extension.fromId(rawExt);

                        if(ext.isEmpty()){
                            System.err.println("Unrecognized Extension "+rawExt);
                            System.exit(1);
                        }
                        exts.remove(ext.get());
                    }
                }else if(arg.equals("--no-exts")||arg.equals("--no-extensions")){
                    exts.clear();
                }else if(arg.equals("--all-exts")||arg.equals("--all-extensions")){
                    exts = EnumSet.allOf(ExtensionsState.Extension.class);
                }else{
                    System.out.println(USAGE);
                    System.exit(1);
                }

            }else if(arg.startsWith("-")){
                if(arg.equals("-o")){
                    if(!iter.hasNext()) {
                        System.err.println("-o needs an argument");
                        System.exit(1);
                    }
                    outputFile = iter.next();
                }else if(arg.startsWith("-o")){
                    outputFile = arg.substring(2);
                }else if(arg.startsWith("-V")){
                    System.out.println("tiny-compiler 1.0");
                    System.out.println("Copyright (C) 2022, Connor Horman. Release under the terms of the BSD 2-clause with patent license");
                }else if(arg.startsWith("-h")){
                    System.out.println(USAGE);
                    System.exit(0);
                }
            }else
                file = arg;
        }
        if(stage==CompilationStage.Codegen)
            optFilter = Functional.and(optFilter, s->true);
        else
            optFilter = Functional.or(optFilter, s->false);
        if(file==null){
            System.err.println("An input file must be provided");
            System.exit(1);
        }

        ExtensionsState extensions = new ExtensionsState(exts);

        try(var in = new BufferedInputStream(file.equals("-")?System.in:new FileInputStream(file))){
            TinyLexer lex = new TinyLexer(file,in,extensions);
            List<Symbol> toks = new ArrayList<>();

            Symbol sym;
            while((sym = lex.nextToken()).getSym()!= TinySym.Eof){
                if(sym.getSym()==TinySym.Error){
                    System.err.println("Error: "+sym);
                    System.exit(1);
                }else if(sym.getSym()==TinySym.EndGroup){
                    System.err.println("Error: Unexpected unmatched ), "+sym.getSpan());
                    System.exit(1);
                }
                toks.add(sym);
            }
            Program prg = ProgramParser.parseProgram(new Peek<>(toks.iterator()),extensions);

            if(stage.compareTo(CompilationStage.Mir)>=0){
                SSAConverter conv = new SSAConverter();
                SSAProgram ssaprg = conv.convertProgram(prg);

                ServiceLoader<Optimizer> opts = ServiceLoader.load(Optimizer.class);

                for(Optimizer opt : opts.stream()
                        .map(Supplier::get)
                        .filter(Functional.mapPredicate(optFilter,Optimizer::getName)).toList()){
                    ssaprg = opt.optimize(ssaprg);
                }

                if(stage==CompilationStage.Codegen){
                    ServiceLoader<CodegenService> codegens = ServiceLoader.load(CodegenService.class);
                    String finalDesiredCodegen = desiredCodegen;
                    CodegenService service = codegens.stream().map(Supplier::get).filter(c-> finalDesiredCodegen ==null||c.matches(finalDesiredCodegen)).findAny().orElseGet(JavaCodegenService::new);
                    int pos = file.lastIndexOf('.');
                    String name = pos<0?file:file.substring(0,pos);
                    if(outputFile==null)
                        outputFile = service.convertFileName(name);
                    if(outputVersion==null)
                        outputVersion = service.defaultOutputVersion();
                    Codegen cg = service.create(name,outputVersion);
                    cg.writeIR(ssaprg);
                    try(var out = new FileOutputStream(outputFile)){
                        cg.writeOutput(out);
                    }
                }else{
                    OutputStream out;
                    if(outputFile==null||outputFile.equals("-")) {
                        out = System.out;
                    }else
                        out = new FileOutputStream(outputFile);
                    PrintStream print = new PrintStream(out);
                    for(SSAMethodDeclaration decl : ssaprg.getDeclarations()){
                        print.println(decl);
                        print.println();
                    }
                    if(out!=System.out)
                        out.close();
                }

            }else{
                OutputStream out;
                if(outputFile==null||outputFile.equals("-")) {
                    out = System.out;
                }else
                    out = new FileOutputStream(outputFile);
                PrintStream print = new PrintStream(out);
                for(MethodDeclaration decl : prg.getDeclarations()){
                    print.print(decl.returnType()+" ");
                    if(decl.isMain())
                        print.print("MAIN ");
                    print.print(decl.getName() +"(");
                    String sep = "";
                    for(Parameter param : decl.getParameters()){
                        print.print(sep);
                        sep =", ";
                        print.print(param);
                    }
                    print.println("){");
                    for(Statement stat : decl.getBlock().getStatements()){
                        print.println(stat);
                    }
                    print.println("}");
                }
                if(out!=System.out)
                    out.close();
            }

        } catch (SyntaxError e) {
            System.err.println("Syntax Error");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
