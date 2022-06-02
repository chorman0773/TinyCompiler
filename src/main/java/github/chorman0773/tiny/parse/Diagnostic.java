package github.chorman0773.tiny.parse;

import github.chorman0773.tiny.ExtensionsState;
import github.chorman0773.tiny.lex.Span;

import java.util.Optional;

public record Diagnostic(String text, Span token, Optional<ExtensionsState.Extension> noteExtension) {

}
