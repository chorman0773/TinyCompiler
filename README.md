# Tiny Language Compiler

Extensible Compiler of the Tiny Language showed in COMP-2140 "Compilers, Translators, and Grammers".

By default, the compiler compiles to JVM Bytecode, but using service providers,
 it is possible to configure the backend run by the program.

## How to use

To run the compiler, use `./gradlew run --args="<path to tiny file>`.

By default, this generates a file in the same directory as the input file, 
 but with the extension `.class` rather than `.tiny`. 
If the codegen is overriden, then that codegen dictates the output extension instead.


To run the resulting program, first the standard library needs to be built with 
 `./gradlew tiny-stdlib:build`. 

Then you can run the program with `java -cp .;tiny-stdlib/build/libs/tiny-stdlib-1.0.jar` (or use `:` instead of `;` on UNIX).

## The Weeds - How does this work

The Tiny Compiler works using an iterative architecture, performing, at a high level, the following steps in order:
1. Lexing
2. Parsing
3. Semantic Analysis and SSA Conversion
4. Optimization
5. Codegen

Lexing is performed by a MM Lexer generated using JLex. It employs some tricks to make parsing easier:
* Keywords and Sigils are passed as a single token type, with the token being passed as a string
* Parenthesis groups are lexed as a single token, containing a list of the enclosed tokens. This process is recursive.

The `github.chorman0773.tiny.lex` package contains the lexer

Parsing is a hand-written LL(1) parser, using the token stream generated by the Compiler.
Other than simple one-lookahead tricks, it also employs pratt parsing for parsing binary operators.

The `github.chorman0773.tiny.parse` package contains the parser, 
 and the `github.chorman0773.tiny.ast` package contains the AST.

Semantic Analysis and SSA Conversion follows lowering TINY local variables into SSA numbered locals.
This pass is interleaved with semantic analysis, as much of that analysis occurs automatically when lowering to SSA.

In Lowering, the following transformations are applied:
* Simple Declarations (that is, declarations without initializers) are removed
* Declarations with initializers and assignment statements are lowered into SSA Declarations, and 
 in the case of reassignment, StoreDead annotations.
* READ statements are desugared into an assignment from a `Read` expression, then lowered as an assignment statement would.
* BLOCK statements are expanded into the component statements 
* IF statements and IF-ELSE statements are expanded into comparing branches and unconditional branches 
* RETURN and WRITE statements are preserved

The Semantic Analyzer is in the package `github.chorman0773.tiny.sema`.

The names of local variables are semantically erased during lowering, and each SSA Declaration is given a corresponding number
(the SSA variables "name"). 
This SSA Local Variable is alive from its declaration until a corresponding `StoreDead` statement. 
SSA Variables cannot be reassigned and are left with the value given to them in the declaration.

IF and IF-ELSE statements function using BasicBlocks, BasicBlocks are numbered, much like locals,
 and contain a number of Lowered SSA Statements. Every Basic Block ends with a terminator, 
 either an unconditional branch, a return statement, or an unreachable statement (not generated by the SSA Conversion).

The unconditional branch statement and the conditional branch statement take a list of SSA Locals
 from the current Basic Block, and map them to a corresponding list from the target basic block.
Those SSA Locals are considered assigned upon entry and may not be reassigned by the block,
 though multiple branch statements can initialize the same SSA Local in the same target basic block,
 and may initialize them to different values.

Except through mapping reassignment, each SSA Local is considered to be owned by the basic block,
 and can neither be read nor written to by another basic block. Locals that are preserved accross basic blocks
 are mapped by the corresponding branch.

The MAIN function declaration is an exception to the terminator rule: 
The last basic block in a declaration marked MAIN may omit the terminator, in which case,
 and implicit `RETURN 0;` statement is inserted.

The SSA Conversion Framework and SSA Types are found in the `github.chorman0773.tiny.sema.ssa` package.

After SSA Conversion, optimizations are performed.
Optimizers are loaded from the class and module path using the service interface `github.chorman0773.tiny.opt.Optimizer`.

Two optimizations are included within the TinyCompiler project itself: Inlining (FIXME: Currently a no-op), and Constant Folding.

All Optimizations run on the SSA form generated by SSA Conversion. They form a map from 
 an incoming SSA-form Program to an outgoing SSA-form program. Optimizations are required to preserve observable behaviour and well-formedness,
 but may alter the structure of the resulting code.

The Inliner selectively choose function expressions, and inlines the function body, expanding the included code out into the surrounding basic block.
This allows both later optimizations to analyze the function body in the context of the calling function, 
 and for expensive `call` instructions to be eliminated from machine code.

Constant Folding works by folding known (constant) values arround pure operations with well-known behaviour, 
 for example, integer and floating-point arithmetic, replacing them with the constant result. 
It also preserves constant values assigned to local variables, so that later expressions reading them can be folded as well.

Other optimizations may be added in the future, and user provided ones may be run as well.

Optimizations can be found in the `github.chorman0773.tiny.opt` package,
and the two implemented ones in `github.chorman0773.tiny.opt.inline` and `github.chorman0773.tiny.opt.fold`.


## Planned Features

- [x] Rewrite of Lexer - If you want something done right, gotta do it yourself
- [ ] Better Diagnostics - As much fun as Throwing an exception on a compiler error is
- [x] Token Spans - For Diagnostics
- [ ] Debug Info Generation - So you can decompile Tiny
- [x] Semantic Analysis Fixes

## Planned Extensions

The Tiny Language is minimal, but powerful. Using the above architecture, 
 the following extensions are planned:
- [x] UAX #31 support (Unicode Identifiers)
- [ ] String Expressions (including String Literal Expressions)
- [ ] IO With Computed Paths
- [x] WHILE loops.
- [x] Additional Comparisons for `IF` and `WHILE` statements
- [ ] Boolean Operators (`||`, `&&`, and `!`)
- [ ] Additional types
- [ ] Libraries and importing.

## Copyright

Copyright (c) 2022 Connor Horman

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

Subject to the terms and conditions of this license, each copyright holder and contributor hereby grants to those receiving rights under this license a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable (except for failure to satisfy the conditions of this license) patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer this software, where such license applies only to those patent claims, already acquired or hereafter acquired, licensable by such copyright holder or contributor that are necessarily infringed by:

(a) their Contribution(s) (the licensed copyrights of copyright holders and non-copyrightable additions of contributors, in source or binary form) alone; or

(b) combination of their Contribution(s) with the work of authorship to which such Contribution(s) was added by such copyright holder or contributor, if, at the time the Contribution is added, such addition causes such combination to be necessarily infringed. The patent license shall not apply to any other combinations which include the Contribution.

Except as expressly stated above, no rights or licenses from any copyright holder or contributor is granted under this license, whether expressly, by implication, estoppel or otherwise.

DISCLAIMER

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

