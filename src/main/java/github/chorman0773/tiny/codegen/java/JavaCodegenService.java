package github.chorman0773.tiny.codegen.java;

import github.chorman0773.tiny.codegen.Codegen;
import github.chorman0773.tiny.codegen.CodegenService;

public class JavaCodegenService implements CodegenService {

    public JavaCodegenService(){} // Explicitly declare default constructors


    @Override
    public boolean matches(String name) {
        return name.equalsIgnoreCase("jvm")||name.equalsIgnoreCase("java");
    }

    @Override
    public boolean outputVersionSupported(String version) {
        int ver = JavaCodegen.parseJavaVersion(version);

        return ver<52||ver>62;
    }

    @Override
    public String defaultOutputVersion(){
        return "1.7";
    }

    @Override
    public String convertFileName(String outputName){
        return outputName+".class";
    }

    @Override
    public Codegen create(String outputFile, String outputVersion) {
        return new JavaCodegen(outputVersion,outputFile);
    }
}
