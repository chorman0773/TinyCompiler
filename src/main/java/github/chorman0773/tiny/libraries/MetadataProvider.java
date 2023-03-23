package github.chorman0773.tiny.libraries;

import github.chorman0773.tiny.sema.ssa.MethodSignature;

import java.util.Optional;
import java.util.stream.Stream;

public interface MetadataProvider {
    public Stream<String> getMethods();
    public Optional<MethodSignature> getSignature(String name);
}
