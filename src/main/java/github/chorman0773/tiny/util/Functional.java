package github.chorman0773.tiny.util;

import java.util.function.*;

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

    public static <T,U> Consumer<Pair<? extends T,? extends U>> join(Consumer<? super T> tConsumer, Consumer<? super U> uConsumer){
        return p->{
            tConsumer.accept(p.first());
            uConsumer.accept(p.second());
        };
    }

    public static <T,U> Supplier<Pair<? super T, ? super U>> join(Supplier<? extends T> tSupplier, Supplier<? extends U> uSupplier){
        return ()->new Pair<>(tSupplier.get(), uSupplier.get());
    }

    public static <T,U> Consumer<Pair<? extends T,? extends U>> group(BiConsumer<? super T, ? super U> cons){
        return p->cons.accept(p.first(),p.second());
    }

    public static <T,U,R> Function<Pair<? extends T,? extends U>,? super R> group(BiFunction<? super T, ? super U, ? extends R> cons){
        return p->cons.apply(p.first(),p.second());
    }

    public static <T,U> BiConsumer<? extends T, ? extends U> expand(Consumer<Pair<? super T, ? super U>> cons){
        return (a,b)->cons.accept(new Pair<>(a,b));
    }

    public static <T,U,R> BiFunction<T,U,R> expand(Function<Pair<? super T, ? super U>,? extends R> cons){
        return (a,b)->cons.apply(new Pair<>(a,b));
    }

    public static <T,R,S,V> Function<T,R> split(BiFunction<S,V,R> gCons,
                      Function<? super T,? extends S> sCons,
                      Function<? super T, ? extends V> vCons){
        return t->gCons.apply(sCons.apply(t),vCons.apply(t));
    }
}
