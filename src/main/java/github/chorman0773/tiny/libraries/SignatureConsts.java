package github.chorman0773.tiny.libraries;

public interface SignatureConsts {

    public static int MAGIC = 0x01020304;
    public static int VERSION = 0x0000;

    public static int TYPE_INVAL = 0x00;

    public static int TYPE_INT = 0x01;
    public static int TYPE_REAL = 0x02;
    public static int TYPE_STRING = 0x03;

    public static int SIGKIND_INVAL = 0x00;
    public static int SIGKIND_METHOD = 0x01;
}
