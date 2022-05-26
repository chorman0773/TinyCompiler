package github.chorman0773.tiny.sema.ssa;

import github.chorman0773.tiny.ast.*;
import github.chorman0773.tiny.sema.ssa.stat.SSAStatement;

import java.util.*;
import java.util.stream.Collectors;

public class SSAMethodDeclaration {
    private final List<Type> params;
    private final Type retTy;
    private final List<BasicBlock> blocks;
    private final boolean isMain;
    private final String name;

    public SSAMethodDeclaration(List<Type> param, boolean isMain, String name, Type retTy, List<BasicBlock> blocks){
        this.params = param;
        this.retTy = retTy;
        this.blocks = blocks;
        this.isMain = isMain;
        this.name = name;
    }

    public List<BasicBlock> getBlocks(){
        return Collections.unmodifiableList(blocks);
    }

    public List<Type> getParameters(){
        return Collections.unmodifiableList(params);
    }

    public Type getReturnType(){
        return retTy;
    }

    public boolean isMain(){
        return isMain;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        StringBuilder st = new StringBuilder();
        st.append("function");
        if(isMain)
            st.append(" MAIN");
        st.append(" ");
        st.append(name);
        st.append("(");
        String sep = "";
        int i = 0;
        for(Type param : params){
            st.append(sep)
                .append("_")
                .append(i++)
                .append(": ")
                .append(param);
            sep = ", ";
        }
        st.append(") -> ").append(retTy);
        st.append("{\n");
        for(BasicBlock bb : blocks){
            st.append("\t")
                .append(bb.getNum())
                .append(": {\n")
                .append("\t[")
                    .append(bb.getLocals().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->"_"+e.getKey()+": "+e.getValue()).collect(Collectors.joining(", "))).append("]\n");


            for(SSAStatement stat : bb.getStats())
                st.append("\t\t").append(stat).append("\n");
            st.append("\t}\n");
        }
        st.append("}\n");
        return st.toString();
    }

}
