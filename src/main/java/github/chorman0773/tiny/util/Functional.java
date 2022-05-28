package github.chorman0773.tiny.util;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Functional {
    public static <T,U> Predicate<T> mapPredicate(Predicate<? super U> pred, Function<? super T,? extends U> func){
        return val->pred.test(func.apply(val));
    }

    public static <T> Predicate<T> and(Predicate<? super T> a, Predicate<? super T> b){
        if(a==null)
            return b::test;
        else if(b==null)
            return a::test;
        return val->a.test(val)&&b.test(val);
    }

    public static <T> Predicate<T> or(Predicate<? super T> a, Predicate<? super T> b){
        if(a==null)
            return b::test;
        else if(b==null)
            return a::test;
        return val->a.test(val)||b.test(val);
    }
}
