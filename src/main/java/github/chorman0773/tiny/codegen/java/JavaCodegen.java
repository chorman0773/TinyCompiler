package github.chorman0773.tiny.codegen.java;

import github.chorman0773.tiny.codegen.Codegen;
import github.chorman0773.tiny.sema.ssa.MethodSignature;
import github.chorman0773.tiny.sema.ssa.SSAMethodDeclaration;
import github.chorman0773.tiny.sema.ssa.SSAProgram;
import github.chorman0773.tiny.sema.ssa.expr.*;
import static github.chorman0773.tiny.ast.Type.*;

import org.objectweb.asm.*;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class JavaCodegen implements Codegen {
    private final ClassWriter writer;
    private final int ver;
    private final String outputName;


    static int parseJavaVersion(String version){
        int val;
        if(version.startsWith("1."))
            val = Integer.parseInt(version.substring(2));
        else
            val = Integer.parseInt(version);
        if(val==0)
            return 45;
        return 44+val;
    }

    public JavaCodegen(String targetVersion,String outputName){
        ver = parseJavaVersion(targetVersion);
        assert 51<ver&&ver<63 : "Internal Compiler Error: Invalid Java Codegen Version "+targetVersion;
        writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        this.outputName = outputName;
    }

    public static Type toJavaType(github.chorman0773.tiny.ast.Type ty){
        return switch(ty){
            case Int -> Type.INT_TYPE;
            case Real -> Type.DOUBLE_TYPE;
            case String -> Type.getObjectType("java/lang/String");
        };
    }

    public static String toJavaSignature(MethodSignature sig){
        StringBuilder ret = new StringBuilder("(");
        for(var param : sig.params())
            ret.append(toJavaType(param).getDescriptor());
        ret.append(")");
        ret.append(toJavaType(sig.ret()).getDescriptor());
        return ret.toString();
    }

    private static final String BOOTSTRAP_SIGNATURE ="(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";


    public static final Handle READ_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/IOBootstraps","read",BOOTSTRAP_SIGNATURE,false);
    public static final Handle WRITE_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/IOBootstraps","write",BOOTSTRAP_SIGNATURE,false);
    public static final Handle EXIT_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, "github/chorman0773/tiny/stdlib/ProcBootstraps","exit",BOOTSTRAP_SIGNATURE,false);
    public static final Handle MAIN_BOOSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/ProcBootstraps","main",BOOTSTRAP_SIGNATURE, false);
    public static final Handle CAST_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, "github/chorman0773/tiny/stdlib/ProcBootstraps","cast",BOOTSTRAP_SIGNATURE,false);
    public static final Handle BINOP_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/ProcBootstraps","binaryOperator",BOOTSTRAP_SIGNATURE,false);
    public static final Handle CMP_BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,"github/chorman0773/tiny/stdlib/ProcBootstraps","compare",BOOTSTRAP_SIGNATURE,false);

    @Override
    public void writeIR(SSAProgram prg) {
        writer.visit(ver,Opcodes.ACC_PUBLIC|Opcodes.ACC_SUPER|Opcodes.ACC_FINAL,outputName,null,"github/chorman0773/tiny/stdlib/TinyFile",null);

        Handle mainTarget = null;


        for(SSAMethodDeclaration decl : prg.getDeclarations()){
            if(decl.isMain()){
                assert mainTarget==null;
                mainTarget = new Handle(Opcodes.H_INVOKESTATIC, outputName,decl.getName(),"()I",false);
            }
            MethodVisitor visit = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, decl.getName(),toJavaSignature(new MethodSignature(decl.getReturnType(),decl.getParameters())),null,null);
            visit.visitCode();
            JavaMethodCodegen mcg = new JavaMethodCodegen(visit,this.outputName);
            mcg.writeMethod(decl);
            visit.visitEnd();
        }
        if(mainTarget!=null){
            MethodVisitor visit = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
            visit.visitCode();
            visit.visitInvokeDynamicInsn(mainTarget.getName(), mainTarget.getDesc(), MAIN_BOOSTRAP);
            visit.visitInvokeDynamicInsn("exit","(I)V",EXIT_BOOTSTRAP);
            visit.visitInsn(Opcodes.RETURN);
            visit.visitMaxs(1,0);
            visit.visitEnd();
        }

        writer.visitEnd();
    }


    @Override
    public void writeOutput(OutputStream stream) throws IOException {
        stream.write(writer.toByteArray());
    }
}
