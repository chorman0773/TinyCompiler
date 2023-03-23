package github.chorman0773.tiny.util;

import java.util.Optional;
import java.util.function.Function;

public record Pair<T,U>(T first, U second) {

    public static <T,U> Optional<Pair<T,U>> zip(Optional<T> first, Optional<U> second){
        if(first.isEmpty()||second.isEmpty())
            return Optional.empty();
        else
            return Optional.of(new Pair<>(first.get(),second.get()));
    }

    public <R,S> Pair<R,S> map(Function<? super T,? extends R> f1, Function<? super U, ? extends S> f2){
        return new Pair<>(f1.apply(this.first()),f2.apply(this.second()));
    }

    public static <T,U> Optional<Pair<T,U>> transpose(Pair<Optional<T>,Optional<U>> pair){
        if(pair.first.isEmpty()||pair.second.isEmpty())
            return Optional.empty();
        else{
            return Optional.of(new Pair<>(pair.first.get(),pair.second.get()));
        }
    }
}
