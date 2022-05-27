package github.chorman0773.tiny.lex;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CodePointIterator implements Iterator<Integer> {
    private Iterator<Character> inner;
    public CodePointIterator(Iterator<Character> utf16){
        this.inner = utf16;
    }

    public boolean hasNext(){
        return inner.hasNext();
    }

    public Integer next(){
        char val = inner.next();
        if(Character.isHighSurrogate(val)){
            if(inner.hasNext()){
                char val2 = inner.next();
                if(!Character.isLowSurrogate(val2))
                    throw new NoSuchElementException("Invalid UTF-16 (unpaired High Surrogate)");
                return 0x10000 + (val-0xD800)<<10 + (val2-0xDC00);
            }else
                return (int)val;
        }else
            return (int)val;
    }

}
