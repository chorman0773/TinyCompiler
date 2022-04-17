package github.chorman0773.tiny.stdlib;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ProcBootstraps {

    static final MethodHandles.Lookup syslookup = MethodHandles.lookup();

    public CallSite exit(MethodHandles.Lookup lookup, String name, MethodType desc) throws NoSuchMethodException, IllegalAccessException {
        return new ConstantCallSite(syslookup.findStatic(java.lang.System.class,"exit",desc));
    }

    public CallSite main(MethodHandles.Lookup lookup, String name, MethodType desc) throws NoSuchMethodException, IllegalAccessException {
        return new ConstantCallSite(lookup.findStatic(lookup.lookupClass(),name,desc));
    }

    public CallSite cast(MethodHandles.Lookup lookup, String name, MethodType desc) throws NoSuchMethodException, IllegalAccessException{
        if(desc.parameterCount()!=1)
            throw new NoSuchMethodException("Cannot invoke cast on "+desc.toMethodDescriptorString()+" cast method must have at most 1 parameter");
        else if(desc.parameterType(0)==desc.returnType())
            return new ConstantCallSite(MethodHandles.identity(desc.returnType()));
        else if(desc.returnType()==String.class)
            return new ConstantCallSite(syslookup.findStatic(String.class,"valueOf",desc));
        else if(desc.parameterType(0)==String.class&&desc.returnType()==int.class)
            return new ConstantCallSite(syslookup.findStatic(Integer.class,"parseInt",desc));
        else if(desc.parameterType(0)==String.class&&desc.returnType()==double.class)
            return new ConstantCallSite(syslookup.findStatic(Double.class,"parseDouble",desc));
        else
            return new ConstantCallSite(MethodHandles.explicitCastArguments(MethodHandles.identity(desc.returnType()),desc));
    }
}
