package github.chorman0773.tiny.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface CollectionsSupport {
    public static <K,V> Stream<Pair<K,V>> entriesStream(Map<? extends K,? extends V> m){
        return m.entrySet().stream().map(e->new Pair<>(e.getKey(),e.getValue()));
    }

    private static <K,V> void mapInsert(Map<K,V> map, Pair<? extends K,? extends V> pair){
        map.putIfAbsent(pair.first(),pair.second());
    }

    public static <K,V> Map<K,V> collectMap(Stream<Pair<? extends K,? extends V>> stream, Supplier<Map<K,V>> mapSupplier){
        return stream.<Map<K,V>>collect(mapSupplier,CollectionsSupport::mapInsert,Map<K,V>::putAll);
    }
}
