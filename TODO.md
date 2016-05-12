WeaC TODO List
===
- [ ] Rename 'Parsing' step to a better fitting name

- [ ] Create improved actual parser
    - [ ] Must support going forward and backwards
    - [ ] Must support to extract a pattern (regular expressions ?)

- [ ] Resolution should fail if a variable is not initialized and requests auto type inferring.

- [ ] More verification steps in order to check the code is valid before trying to compile/resolve/precompile it

- [ ] Improve unit tests
    - [ ] Preprocessor
    - [ ] 'Parsing'
    - [ ] Precompilation
    - [ ] Resolution
    - [ ] Compilation

- [ ] Multi-target compatibility
    - [x] Java Virtual Machine
    - [ ] Spir-V

- [ ] Replace '>' (equivalent of Java 'extends' / 'implements') by more easily resolvable token (causes problems with generic types)

- [ ] Don't perform type erasure on generic types until compilation (if needed)
    - [ ] Type Resolver must take generic types in account

- [ ] Intermediate representation to allow to switch from a compilation target to another

- [ ] Work on struct types

- [ ] Data classes ?

- [ ] Operator overloading
    - [ ] Allow new operators to be created
    - [ ] Find a way to support something like: notOverloadedOperatorType {operator} overloadedOperatorType where 'overloadedOperatorType' decides of the result
    - [x] Base operators overloading

- [ ] Type System supporting union and intersect types (this information may be lost in the compilation step) (Not sure yet)

- [ ] Finish specifications