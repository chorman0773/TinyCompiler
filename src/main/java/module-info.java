module github.chorman0773.tiny {
    exports github.chorman0773.tiny.ast;
    exports github.chorman0773.tiny.codegen;
    exports github.chorman0773.tiny.opt;
    exports github.chorman0773.tiny.sema.ssa;

    requires org.objectweb.asm;

    uses github.chorman0773.tiny.codegen.CodegenService;
    uses github.chorman0773.tiny.opt.Optimizer;

    provides github.chorman0773.tiny.codegen.CodegenService with github.chorman0773.tiny.codegen.java.JavaCodegenService;
}