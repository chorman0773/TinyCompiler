package github.chorman0773.tiny.libraries;

import github.chorman0773.tiny.sema.ssa.MethodSignature;
import static github.chorman0773.tiny.util.CollectionsSupport.*;
import static github.chorman0773.tiny.util.Functional.*;
import github.chorman0773.tiny.util.Pair;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LibraryMetadata implements MetadataProvider {
    private final Map<String, MethodSignature> signatures;

    public LibraryMetadata(Map<String,MethodSignature> signatures){
        this.signatures = signatures;
    }

    public LibraryMetadata(MetadataProvider provider){

        signatures = collectMap(provider.getMethods()
                .map(split(Pair::zip,Optional::of,provider::getSignature))
                .flatMap(Optional::stream),
                HashMap::new);
    }

    @Override
    public Stream<String> getMethods() {
        return signatures.keySet().stream();
    }

    @Override
    public Optional<MethodSignature> getSignature(String name) {
        return Optional.ofNullable(signatures.get(name));
    }
}
