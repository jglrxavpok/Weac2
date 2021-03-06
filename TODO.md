WeaC TODO List
===
   
- [ ] Loops
    - [ ] For loop
    - [ ] While loop
    - [ ] Until loop

- [ ] Support for native code
    - [ ] JVM
    - [ ] Spir-V
    - [ ] Use of .dll-like files ?
    
- [ ] 'switch'
    
- [ ] Use correct line in error messages

- [ ] Complete migration to the new parser

- [ ] More verification steps in order to check the code is valid before trying to compile/resolve/precompile it

- [ ] Improve unit tests
    - [ ] Preprocessor
    - [ ] Chopping
    - [ ] Precompilation
    - [ ] Resolution
    - [ ] Compilation

- [ ] Multi-target compatibility
    - [x] Java Virtual Machine
    - [ ] Spir-V

- [ ] Better generics handling
    - [ ] Don't perform type erasure on generic types until compilation (if needed)
        - [ ] Type Resolver must take generic types in account
    - [ ] More intelligent generic parameters identification (avoid confusion with 'superclass keyword' '>')

- [ ] Intermediate representation to allow to switch from a compilation target to another

- [ ] Work on struct types

- [ ] Data classes ?

- [ ] Operator overloading
    - [x] Base operators overloading
    - [ ] Allow new operators to be created
    - [ ] Find a way to support something like: notOverloadedOperatorType {operator} overloadedOperatorType where 'overloadedOperatorType' decides of the result

- [ ] Type System supporting union and intersect types (this information may be lost in the compilation step) (Not sure yet)

- [ ] Default argument values

- [ ] Finish specifications

Done
===
- [x] Rename 'Parsing' step to a better fitting name: Chopping
    - [x] Apply new name
    
- [x] Resolution should fail if a variable is not initialized and requests auto type inferring.

- [x] Create improved actual parser
    - [x] Must support going forward and backwards
    - [x] Must support extracting a pattern
    
- [x] Auto type inference
    - [x] Fields
    - [x] Local variables
    
- [x] Allow to access class (like "Object.class" in Java)