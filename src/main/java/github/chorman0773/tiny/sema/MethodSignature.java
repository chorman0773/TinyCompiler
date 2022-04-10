package github.chorman0773.tiny.sema;

import github.chorman0773.tiny.ast.Type;

import java.util.List;

public record MethodSignature(Type ret, List<Type> params) {
}
