# Tiny Middle Intermediate Representation

The TinyCompiler translates tiny source code to a middle intermediate representation, described herein.

## Grammar

Note: the TinyCompiler does not accept mir programs directly, but can generate them using `--dump=mir`

Lexical Grammar is the same as the tiny source language with the following modifications:
```abnf
identifier := (<XID_Start> / "_")[*(<XID_Part> / "_")]
integer := *(%x30-39)

sigil /= "->" / "=>" / ":"

paren-group /= "[" <token-stream> "]" / "{" <token-stream> "}"
```

Additionally, the keyword production is not used (all eligible identifiers lex as the identifier production).


```abnf
ssa-program := [*<ssa-method-decl>]

ssa-method-decl := "function" ["MAIN"] <identifier> "(" <ssa-parameters> ")" "->" <type> "{" *<basic-block> "}

ssa-parameter := <ssa-local> ":" <type>
ssa-parameters := <ssa-parameter> / (<ssa-parameter> "," <ssa-parameters>)

basic-block := <number>":" "{" [*<ssa-statement>] <ssa-terminator> "}"

ssa-terminator := "unreachable" / "return <ssa-expr>" / "branch" <ssa-branch>

ssa-branch := "@"<number> "[" [<ssa-local-remaps>] "]"

ssa-local-remap := (<ssa-local> "=>" "<ssa-local>") 

ssa-local-remaps := <ssa-local-remap> / <ssa-local-remap> "," <ssa-local-remaps>

ssa-local := "_"<integer>

ssa-expr := <ssa-local> 
    / <identfier> "(" <ssa-arguments> ")" 
    / "Read" "(" <string> ")" "as" <type>
    / "(" <ssa-expr> ")" "as" <type>
    / "(" <ssa-expr> ")"
    / <number>

ssa-statement := <ssa-local> ":" <type> ":=" <ssa-expr>
    / "nop" 
    / "discard" <ssa-expr> 
    / "write" "(" <ssa-expr>, <string> ")"
    / "branch" "compare" "(" <ssa-bool-expr> ")" <ssa-branch>
    / "storage" "dead" "(" <ssa-local> ")"
```


## Basic Blocks

Each function is made up of one or more basic blocks. Basic Blocks are indivisible units of execution 
 a program can only begin executing a basic block from the start.

Every basic block contains zero or more statements followed by exactly one terminator
(either `unreachable`, a return statement, or an unconditional `branch`).

Every basic block is numbered, in increasing order. 
The first basic block in the function is basic block `0`.

## Locals

Locals in MIR are numbered SSA values. 
Local names are numbered starting from `0` and are strictly increasing. 
Local names are unique to a particular basic block, 
 a local declared in one basic block is unavailable in others.

Before an expression uses the name of a local variable within a basic block, 
 it shall appear in exactly one of the following contexts, without a later `storage dead` state naming that local:
* Exactly one declaration statement preceeding the use
* A declaration in a local remaps in every branch to the basic block and, in basic block `0`, a declaration in the parameter list of the function

Locals are given a single value: a local variable may not appear in multiple declarations, 
 or in both a declaration and either as a parameter or in a local remap list.

In branches to a basic block, 
 local names in the current basic block can be remapped to names in the target basic block.

The `storage dead` statement indicates that a local name is unused. 
After a `storage dead` statement that names a particular local, 
that name cannot be referenced in an expression or remap.

## Terminators

Each basic block ends with exactly one terminator.

The following statements are terminator:
* `return`
* `unreachable`
* `branch`

### Return Statements

`ssa-terminator := "return" <ssa-expr>`

Returns a value from the current function. 
 Control is transferred out of the current block and function and into the caller.

### Unreachable Statement
 
`ssa-terminator := "unreachable"`

Indicates that the statement is unreachable.

The behaviour of a program that executes this statement is undefined.
In determining whether the statement is executed, 
 it may be assumed that all `write` statements and `read` expressions do not fail at runtime.


### Unconditional Branch

`ssa-terminator := "branch" @<number> "["<ssa-local-remaps>"]"`

Branches to the given numbered basic block, 
 remapping each local variable given in the `ssa-local-remaps` from this basic block 
 to the corresponding local variable in the destination basic block.

After remapping,execution continues at the beginning of the basic block given by `<number>`.

## Statements

### No-operation

`ssa-statement := nop`

Performs no operation. 

### Discard Value

`ssa-statement := discard <ssa-expr>`

Evaluates `ssa-expr` for its side effects and discards the result.

### Declaration

`ssa-statement := <ssa-local> ":" <type> ":=" <ssa-expr>`

Declares a new local of the give type and initializes it to the result of the given expression

### Write

`ssa-statement := "write" "(" <ssa-expr> "," <string> ")"`

Writes the value of `<ssa-expr>` to the file given by `<string>`.

If the file is unavailable, the program terminates unsuccessfully, 
 possibly after printing an unspecified diagnostic message.

### Comparing Branch

`ssa-statement := branch compare <ssa-bool-expr> "@"<integer> "["<ssa-local-remaps>"]"`

evaluates the boolean expression, and if the condition is satisfied, 
 remaps the locals in `<ssa-local-remaps>` then branches to the basic block designated by `<integer>`.

`ssa-bool-expr := "("<ssa-expr> ("!="/"==") <ssa-expr>")"`

the `branch` compare instruction can perform both equality and inequality comparisons of expresions.

The condition is satisfied if the left expression compares equal or unequal to the right expression.


## Expressions


