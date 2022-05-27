# Language Spec

The implemented language is derived from the language used in COMP-2140 (Languages, Translators, and Grammars)
 but includes added semantic behaviour, as well as a few extensions.

## Lexical Grammar

The following Augmented BNF (ABNF defined by [[RFC 5234]](https://datatracker.ietf.org/doc/html/rfc5234)) describes the lexical grammar

```bnf
token := <literal>\ <keyword>|<identifier>|<sigil>|<paren-group>

token-stream := *([<whitespace>]<token>) [<whitespace>]

ANY_CHAR :=  %x01-0C \ (%x0D%0x0A) \ %x0E-7F \ %x80-D7FF \ %xE000-10FFFF

whitespace := <WSpace> \ <comment> \ <whitespace><whitespace>

comment := "/**" [*<ANY_CHAR>] "**/"

QUOTE := %x22

STRING_CHAR := %x01-09 \ %x0B-0C \ %x0E-21 \  %x23-7F \ %x80-D7FF \ %xE000-10FFFF

string-literal := <QUOTE> [*<STRING_CHAR>] <QUOTE>

DIGIT := %x30-39

number-literal := *<digit>["." *<digit>]

literal := <number-literal> / <string-literal>

identifier := <XID_Start>[*<XID_Part>]

keyword := "INT" / "REAL" / "STRING" / "IF" / "ELSE" / "BEGIN" / "END" / "MAIN" / "READ" / "WRITE"

sigil := "/" / "*" / "+" / "-" / ":=" / "=" / "==" / "!=" / "," / ";"

paren-group := "(" <token-stream> ")"

file := <token-stream>
```

1. A well-formed program matches the `file` lexical production.
2. A program that contains a consecutive `/` and `*` token shall separate those tokens with a whitespace
3. Each token shall be lexed the largest sequence of consecutive characters that forms a valid token
4. If a token matches both the `keyword` and `identifier` production, then it shall be lexed as a `keyword`.
5. The nonterminals `XID_Start`, `XID_Part`, and `WSpace` refer to unicode characters with that property,
 except that U+000D (Carriage Return) shall only appear followed by U+000A (Newline).

## Syntactic Grammar

The following ABNF describes the Syntactic Grammar of the language

```bnf 
file := *<method-decl>

method-decl := <type> ["MAIN"] <identifier> "(" <param-list> ")" <block>

type := "INT" / "STRING" / "REAL"

param-list := [<param> / (<param> "," <param-list>)]

param := <type> <identifier>

block := "BEGIN" [*<statement>] "END"

statement := <declaration> / <assignment> / <read> / <write> / <if> / <block> / <return>

declaration := <type> <identifier> [":=" <expression>] ";"
assignment := <identifier> ":=" <expression> ";"
read := "READ" "(" <identifier> "," <string-literal> ")" ";"
write := "WRITE" "(" <expression> "," <string-literal> ")" ";"
if := "IF" "(" <bool-expression> ")" <statement> ["ELSE" <statement>]
return := "RETURN" <expression> ";"

bool-expression := (<expression> ("==" / "!=") <expression>)

```