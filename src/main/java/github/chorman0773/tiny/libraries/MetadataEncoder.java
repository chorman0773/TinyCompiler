package github.chorman0773.tiny.libraries;

import github.chorman0773.tiny.ast.Type;
import github.chorman0773.tiny.sema.ssa.MethodSignature;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MetadataEncoder implements DataOutput, AutoCloseable, SignatureConsts {

    private final DataOutput base;

    public MetadataEncoder(DataOutput base) {
        this.base = base;
    }

    @Override
    public void write(int b) throws IOException {
        this.base.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.base.write(b,off,len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        this.base.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        this.base.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        this.base.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        this.base.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.base.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.base.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.base.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.base.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        this.base.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        this.base.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        this.base.writeUTF(s);
    }


    public void writeUTF8Bytes(String s) throws IOException{
        this.write(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws Exception {
        if(base instanceof AutoCloseable ac)
            ac.close();
    }

    public void writeType(Type t) throws IOException{
        switch(t){
            case Int -> this.writeShort(TYPE_INT);
            case Real -> this.writeShort(TYPE_REAL);
            case String ->  this.writeShort(TYPE_STRING);
        }
    }

    public void writeSignature(MethodSignature sig) throws IOException{
        this.writeShort(SIGKIND_METHOD); // method
        this.writeType(sig.ret());

        var len = sig.params().size();

        if(len>65535)
            throw new IOException("Invalid length of signature - max of 2^16-1 parameter count exceeded");
        this.writeShort(len);

        for(var param : sig.params())
            this.writeType(param);
    }

    public void writeSignaturePair(String name, MethodSignature sig) throws IOException{
        var len = name.length();

        if(len>65535)
            throw new IOException("Invalid length of signature - max of 2^16-1 parameter count exceeded");

        this.writeShort(len);

        this.writeUTF8Bytes(name);

        if((len&1)!=0)
            this.write(0);

        writeSignature(sig);
    }
}
