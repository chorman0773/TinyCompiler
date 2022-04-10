package github.chorman0773.tiny.util;

import java.util.Iterator;
import java.util.Optional;

public class Peek<E> implements Iterator<E> {
    private E peeked;
    private Iterator<? extends E> inner;

    public Peek(Iterator<? extends E> inner){
        this.inner = inner;
    }

    /**
     * Peeks one element of the underlying iterator, and returns it without removing it
     * @return An optional containing the element that will be returned by a subsequent call to {@link #next()}, or an empty optional if no other elements remain
     */
    public Optional<E> peek(){
        if (peeked==null){
            if(inner.hasNext())
                peeked = inner.next();
        }
        return Optional.ofNullable(peeked);
    }

    @Override
    public boolean hasNext() {
        if(peeked != null)
            return true;
        else
            return inner.hasNext();
    }

    @Override
    public E next() {
        if(peeked != null){
            E ret = peeked;
            peeked = null;
            return ret;
        }else
            return inner.next();
    }

    public Optional<E> optNext(){
        if(!this.hasNext())
            return Optional.empty();
        else
            return Optional.of(this.next());
    }
}
