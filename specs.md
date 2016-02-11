WeaC Specifications
=========
Table of contents
====
TODO
___

Authors
---
'jglrxavpok'
____

Preface
--------
WeaC (Abbreviation of Weak C) is a language attempting to mix C, Java and Scala concepts with a as little and simple as possible syntax. The language is still an experiment in order to learn how to make a compiler. It runs on the Java Virtual Machine and is designed to be used along side Java classes.
____

Syntax
---
WeaC languages are written in UTF-8 (expansion to Unicode planned). In contrary to Java or Scala, though they are supported in strings and character literals, Unicode escape characters are not supported inside code.

There are three big categories of tokens:

* **Keywords and identifiers:** 
These tokens are started by a valid identifier start character. 

> Examples
> 
> #### Valid identifiers
> 
> ```java
> Object, x, _, __someValue__, abc0145, afgy789dz, ANSWER_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING, MyArray[]
> ```
> 
> #### Keywords
> 
> ```scala
class, struct, public, protected, private, import, as, return, this, super, object, mixin, new, compilerspecial, package
```

* **Numbers**:
Numbers are tokens that start by a digit.
Number are by default written in base 10. Multiple base are supported by WeaC, here are how they are recognized:

| Base                           | Example               |
| :---------------                |----------------------:|
| 2 (Binary)                     | 0b1010101             |
| 16 (Hexadecimal)               | 0xCAFEBABE            |
| 8 (Octal)                      | 0o17451354            |
| C (Custom, up to 64)           | 0c3#120102, 0c5#10445 |
| (This limit will be modifiable)| (Base 3, Base 5)      |


* **Operators and special characters**:
___

> Specifications written by 'jglrxavpok' and inspired by [Scala 2.11's](http://www.scala-lang.org/files/archive/spec/2.11/).

> Written with [StackEdit](https://stackedit.io/).