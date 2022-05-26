package github.chorman0773.tiny.codegen;

public interface CodegenService {
    public boolean matches(String name);
    public boolean outputVersionSupported(String version);

    public String defaultOutputVersion();

    public String convertFileName(String outputName);

    public Codegen create(String outputFile, String outputVersion);
}
