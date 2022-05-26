package github.chorman0773.tiny;

import github.chorman0773.tiny.ast.Program;
import github.chorman0773.tiny.codegen.Codegen;
import github.chorman0773.tiny.codegen.CodegenService;
import github.chorman0773.tiny.codegen.java.JavaCodegenService;
import github.chorman0773.tiny.lex.Symbol;
import github.chorman0773.tiny.lex.TinyScanner;
import github.chorman0773.tiny.lex.TinySym;
import github.chorman0773.tiny.opt.Optimizer;
import github.chorman0773.tiny.parse.ProgramParser;
import github.chorman0773.tiny.parse.SyntaxError;
import github.chorman0773.tiny.sema.ssa.SSAConverter;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.util.Peek;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) throws IOException {
        String file = args[0];

        try(var in = new BufferedInputStream(new FileInputStream(file))){
            TinyScanner lex = new TinyScanner(in);
            List<Symbol> toks = new ArrayList<>();


            Symbol sym;
            while((sym = lex.yylex()).getSym()!= TinySym.Eof){
                if(sym.getSym()==TinySym.Error){
                    System.err.println("Error on token Stream");
                    System.exit(1);
                }
                toks.add(sym);
            }
            System.out.println(toks);
            Program prg = ProgramParser.parseProgram(new Peek<>(toks.iterator()));
            System.out.println(prg);
            SSAConverter conv = new SSAConverter();
            SSAProgram ssaprg = conv.convertProgram(prg);
            System.out.println(ssaprg);

            ServiceLoader<Optimizer> opts = ServiceLoader.load(Optimizer.class);

            for(Optimizer opt : opts){
                System.out.println("Running Pass " + opt);
                ssaprg = opt.optimize(ssaprg);
                System.out.println(ssaprg);
            }

            ServiceLoader<CodegenService> codegens = ServiceLoader.load(CodegenService.class);
            CodegenService service = codegens.stream().findAny().map(ServiceLoader.Provider::get).orElseGet(JavaCodegenService::new);
            int pos = file.lastIndexOf('.');
            String name = pos<0?file:file.substring(0,pos);
            String outputFile = service.convertFileName(name);
            String outputVersion = service.defaultOutputVersion();
            Codegen cg = service.create(name,outputVersion);
            cg.writeIR(ssaprg);
            try(var out = new FileOutputStream(outputFile)){
                cg.writeOutput(out);
            }
        } catch (SyntaxError e) {
            System.err.println("Syntax Error");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
