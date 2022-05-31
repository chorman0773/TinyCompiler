package github.chorman0773.tiny;

import github.chorman0773.tiny.lex.TinySym;

import java.util.EnumSet;
import java.util.Optional;

public class ExtensionsState {
    public enum Extension{
        Uax31,
        While;


        public static Optional<Extension> fromId(String id){
            return switch(id){
                case "uax31" -> Optional.of(Uax31);
                case "while" -> Optional.of(While);
                default -> Optional.empty();
            };
        }
    }
    private final EnumSet<Extension> exts;

    public ExtensionsState(EnumSet<Extension> exts){
        this.exts = exts;
    }

    public static EnumSet<Extension> defaultExtensions(){
        return EnumSet.of(Extension.Uax31);
    }

    public boolean isIdentifierStart(int codep){
        if(exts.contains(Extension.Uax31))
            return Character.isUnicodeIdentifierStart(codep);
        else{
            return ('A'<=codep&&codep<='Z')||('a'<=codep&&codep<='z');
        }
    }

    public boolean isIdentifierPart(int codep){
        if(exts.contains(Extension.Uax31))
            return Character.isUnicodeIdentifierPart(codep);
        else{
            return ('A'<=codep&&codep<='Z')||('a'<=codep&&codep<='z')||('0'<=codep&&codep<='9');
        }
    }

    public boolean hasExtension(Extension ext){
        return this.exts.contains(ext);
    }


    public boolean isKeyword(String id){
        return switch(id){
            case "INT", "REAL", "STRING", "MAIN", "READ", "WRITE", "BEGIN", "END", "IF", "ELSE", "RETURN" -> true;
            case "WHILE","DO" -> exts.contains(Extension.While);
            default -> false;
        };
    }
}
