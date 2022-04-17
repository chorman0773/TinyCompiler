package github.chorman0773.tiny.stdlib;

import java.lang.invoke.*;

import static github.chorman0773.tiny.stdlib.ProcBootstraps.syslookup;

public class IOBootstraps {

    private static final MethodHandle readInt;
    private static final MethodHandle readDouble;
    private static final MethodHandle readString;

    private static final MethodHandle writeInt;
    private static final MethodHandle writeDouble;
    private static final MethodHandle writeString;

    static {
        try {
            readInt = syslookup.findStatic(IO.class,"readInt", MethodType.fromMethodDescriptorString("(Ljava/lang/String;)I",IOBootstraps.class.getClassLoader()));
            readDouble = syslookup.findStatic(IO.class,"readDouble", MethodType.fromMethodDescriptorString("(Ljava/lang/String;)D",IOBootstraps.class.getClassLoader()));
            readString = syslookup.findStatic(IO.class,"readString", MethodType.fromMethodDescriptorString("(Ljava/lang/String;)Ljava/lang/String;",IOBootstraps.class.getClassLoader()));
            writeInt = syslookup.findStatic(IO.class,"write",MethodType.fromMethodDescriptorString("(Ljava/lang/String;I)V",IOBootstraps.class.getClassLoader()));
            writeDouble = syslookup.findStatic(IO.class,"write",MethodType.fromMethodDescriptorString("(Ljava/lang/String;D)V",IOBootstraps.class.getClassLoader()));
            writeString = syslookup.findStatic(IO.class,"write",MethodType.fromMethodDescriptorString("(Ljava/lang/String;Ljava/lang/String;)V",IOBootstraps.class.getClassLoader()));
        } catch(NoSuchMethodException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    public static CallSite read(MethodHandles.Lookup lookup,String name, MethodType ty) throws NoSuchMethodException {
        if(ty.parameterCount()!=1)
            throw new NoSuchMethodException("Cannot call read without one parameter");
        if(ty.returnType()==int.class)
            return new ConstantCallSite(readInt);
        else if(ty.returnType()==double.class)
            return new ConstantCallSite(readDouble);
        else if(ty.returnType()==String.class)
            return new ConstantCallSite(readString);
        else
            throw new NoSuchMethodException("Bad descriptor on \"read\" "+ty+". Expected return of int, double, or String");
    }

    public static CallSite write(MethodHandles.Lookup lookup, String name, MethodType ty) throws NoSuchMethodException{
        if(ty.parameterCount()!=2)
            throw new NoSuchMethodException("Cannot call read without one parameter");
        if(ty.parameterType(1)==int.class)
            return new ConstantCallSite(writeInt);
        else if(ty.parameterType(1)==double.class)
            return new ConstantCallSite(writeDouble);
        else if(ty.parameterType(1)==String.class)
            return new ConstantCallSite(writeString);
        else
            throw new NoSuchMethodException("Bad descriptor on \"write\" "+ty+". Expected parameter 2 to be either int, double, or String");
    }
}
