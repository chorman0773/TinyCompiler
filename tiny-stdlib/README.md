# Tiny Standard Runtime Library

This library is a minimal ABI that the Java Codegen of the Tiny Compiler assumes exists. 
It is considered ABI stable.

The library is compatible with Java 1.7. 

## Bootstrap Methods

Each interface in the Standard Library is provided via bootstrap methods for 
`invokedynamic` call sites. 
Consumers of the standard library are expected to generate `invokedynamic` instructions, 
 rather than invoking the exposed interfaces directly. 
Other interfaces that may be accessed via the reflection or `java.lang.invoke`,
 or that are accidentally exposed to the direct class interface 
 are not considered stable.

Each bootstrap method has a required signature, and designates the name that should be 
 to achieve the desired behaviour.

Any bootstrap method described here has undefined behaviour at runtime
if used with an improperly generated `invokedynamic` instruction.

The bootstrap methods are considered ABI stable, but do not make any guarantees about the API.
For example, bootstrap methods may declare any checked exception, and these exceptions may change 
within a minor version.

### Notation

In this section, an elaborated form of static method declaration is used, 
 where the unqualified name of the bootstrap method is prefixed by the fully-qualified 
 source name of the class it resides in. 
Each declaration is terminated by a semicolon, then followed by a prose specification 
 of the bootstrap method's behaviour.

Classes in the package `java.lang.invoke` should be deemed as automatically imported,
 as well as classes in the package `java.lang`. 
Other packages always use fully qualified names in signatures.

Specifications following "Expected Name:" indicate call-site names expected by the bootstrap method.
 The special name `any` indicates that any name may be given. The rules for the name may then (optionally) be placed in parenthesis.

Specifications following "Expected Signature:" indicate call-site descriptors expected by the bootstrap method.
The signatures are given in binary form (including binary class names).

Specifications following "Effects:" refer to the effects that result from a successful `invokedynamic`
 that reference the bootstrap method properly. 
The effects are listed in prose, and use source names for java types.

### Startup Procedures

`CallSite github.chorman0773.tiny.stdlib.ProcBootstraps.main(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: any (the name of the main function in the current module, if any)  
Expected Signature: `()I`

Effects: Performs any startup needed by the Standard Library, 
then invokes the method in the current module with the name of the call site using the signature `()I`.

`CallSite github.chorman0773.tiny.stdlib.ProcBootstraps.exit(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `exit`  
Expected Signature: `(I)V`

Effects: Performs any shutdown needed by the Standard Library,
 then exits the program with the `status` given as the argument, as though by `System.exit(status);`

### Runtime Dynamic Operations

`CallSite github.chorman0773.tiny.stdlib.ProcBootstraps.cast(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `cast`  
Expected Signature: Any of `(I)I`, `(D)D`, `(I)D`, `D(I)`, `(I)Ljava/lang/String;`,
 `(D)Ljava/lang/String;`, `(Ljava/lang/String;)I`, `(Ljava/lang/String;)D`, or `(Ljava/lang/String;)Ljava/lang/String;`.

Effects: 
Performs the corresponding type cast based on the parameter and return types:
* If the return type and the parameter type are the same, returns the parameter verbatim.
* If the return type is `int` and the parameter type is `double`, 
 converts the argument as though by a narrowing conversion from `double` to `int`.
* If the return type is `double` and the parameter type is `int`, 
 converts the argument as though by a widening conversion from `int` to `double`.
* If the return type is java.lang.String, 
 converts the argument as though by the appropriate overload of `String.valueOf()`
* If the parameter type is `java.lang.String` and the return type is `int`, 
 converts the argument `arg` as though by `Integer.parseInt(arg)`
* If the parameter type is `java.lang.String` and the return type is `double`,
  converts the argument `arg` as though by `Double.parseDouble(arg)`


`CallSite github.chorman0773.tiny.stdlib.ProcBootstraps.binop(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `add`, `sub`, `mul`, or `div`.
Expected Signature: `(II)I`, `(DD)D`, or `(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;`.

Effects:
Performs the operation given by the callsite name and the operation type:
* If the operation type is `java.lang.String`, then the callsite name shall be `add` or the behaviour is undefined. 
 Concatenates the two parameters as though by `String.concat()`
* If the operation type is `int` or `double`, then performs the corresponding primitive computation given by the callsite name
 where `add` is the primitive operator `+`, `sub` is the primitive operator `-`, `mul` is the primitive operator `*`, and `div` is the primitive operator `/`.

`CallSite github.chorman0773.tiny.stdlib.ProcBootstraps.cmp(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `cmp`
Expected Signature: `(II)I`, `(DD)I`, or `(Ljava/lang/String;Ljava/lang/String;)I`

Effects:
Performs valuewise comparison between the two parameters, returning a negative value, 
 positive value, or zero depending on whether the first parameter is less than, greater than, or equal to.
* For `int`, comparison is signed.
* For `double`, comparison is according to the IEEE partial order. If either is NaN, `-1` is returned [Note: This is consistent with the `dcmpl` instruction].
* For `String`, comparison is according to `String.compareTo`. Comparison is case-sensitive.

### IO Operations

`CallSite github.chorman0773.tiny.stdlib.IOBootstraps.read(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `read`  
Expected Signature: `(Ljava/lang/String;)I`, `(Ljava/lang/String;)D`, or `(Ljava/lang/String)Ljava/lang/String;`

Effects:
Reads from the file given as the parameter and parses it as the return type.
Throws an `IOException` if reading fails or the file is not found.

`CallSite github.chorman0773.tiny.stdlib.IOBootstraps.write(MethodHandles.Lookup lookup, String name, MethodType signature);`

Expected Name: `write`  
Expected Signature: `(Ljava/lang/String;I)V`, `(Ljava/lang/String;D)V`, `(Ljava/lang/String;Ljava/lang/String;)V`

Effects:
Writes the second parameter to the file given as the first parameter.
Throws an `IOException` if writing fails or the file is not found and cannot be created.