package github.chorman0773.tiny.stdlib;

import java.io.IOException;
import java.io.OutputStream;

public class Cleanup {

    public static void exit(int code) throws IOException {
        for(OutputStream out : IO.outputs.values())
            out.close();

        System.exit(code);
    }
}
