package github.chorman0773.tiny.lex;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReadIterator implements Iterator<Character> {
    private BufferedReader buf;
    private int val;
    public ReadIterator(BufferedReader buf){
        this.buf = buf;
        this.val = -2;
    }

    private void tryReadOne(){
        try {
            this.val = buf.read();
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean hasNext() {
        if (this.val == -2)
            tryReadOne();
        return this.val!=-1;
    }

    @Override
    public Character next() {
        if(this.val == -2)
            tryReadOne();

        int val = this.val;
        this.val = -2;
        if(val==-1)
            throw new NoSuchElementException("No more characters on stream");
        return (char)val;
    }
}
