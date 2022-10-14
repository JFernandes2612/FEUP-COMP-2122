# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Group 1G:

| Name           | Number    | Grade | Contribution |
|----------------|-----------|-------|--------------|
| Joel Fernandes | 201904977 | 20    | 100%         |

## Global Grade of the Project:
20

## Summary:

Our tool is a compiler that can be used to compile a program written in the Jmm (Java Minus Minus) language.


## Semantic Analysis:

[Attribution Semantic Analyzer](./src/pt/up/fe/comp/analysers/AttributionSemanticAnalyser.java) is used to check if the attribution is valid. Checks the variable type of the assigned and assignee.

[BinOp Semantic Analyzer](./src/pt/up/fe/comp/analysers/BinOpSemanticAnalyser.java) is used to check if the binary operation is valid. Checks the type of the left and right operands is semanticly correct for each operation.

[Conditional Semantic Analyzer](./src/pt/up/fe/comp/analysers/ConditionalSemanticAnalyser.java) is used to check if the conditional is valid. Checks if the type inside de condicional is valid (is of the *boolean* type).

[Index Semantic Analyzer](./src/pt/up/fe/comp/analysers/IndexSemanticAnalyser.java) is used to check if indexing  a list is valid as well as if the usage of the length method is valid. Checks if the type of the indexing is valid (is of the integer type). Checks if the *length method and indexing is used correctly in an array.

[Method Semantic Analyzer](./src/pt/up/fe/comp/analysers/MethodSemanticAnalyser.java) is used to check if the method call is valid as well as check the return value of it. Checks if the method call has valid parameters and return type according to the symbol table.

[Variable Semantic Analyzer](./src/pt/up/fe/comp/analysers/VariableSemanticAnalyser.java) is used to check if the variable is valid. Checks if the variable is declared and used in a valid context. This analizer also implements the types for diferents types of literals and non binary operations - *this*, *boolean*, *int*, *array*, *class* and the *not* operations for *booleans*.

[Verify No Class Field Overload](./src/pt/up/fe/comp/analysers/VerifyNoClassFieldOverload.java) is used to check if the class has any invalid overloaded fields.

[Verify No Identity Overloading](./src/pt/up/fe/comp/analysers/VerifyNoIdentityOverloading.java) is used to check if the class has any invalid overloaded indentities in a method. There cannot be multiple variables with the same identifier within one method (except *main* because it can contain variables with field names since it is a static method).

[Verify No Method Args Overload](./src/pt/up/fe/comp/analysers/VerifyNoMethodArgsOverload.java) is used to check if the class has any invalid overloaded method arguments.

[Verify No Method Overload](./src/pt/up/fe/comp/analysers/VerifyNoMethodOverload.java) is used to check if the class has any invalid overloaded methods.

[Verify No Method Locals Overload](./src/pt/up/fe/comp/analysers/VerifyNoMethodLocalsOverload.java) is used to check if the class has any invalid overloaded method locals.

[Verify No Field Overload](./src/pt/up/fe/comp/analysers/VerifyNoFieldOverload.java) is used to check if the class has any invalid overloaded fields.

## Code Generation:

Our tool generates code for the Jmm language via a parser and a conversion from AST to direct Jasmin code which is compiled an run after using another tool. [AST to Jasmin](./src/pt/up/fe/comp/visitors/AstToJasminVisitor.java) is the source code for this last conversion.

## Pros:

- Supports every Jmm language feature.
- Compiles and runs fast.
- Has some optimizations on the AST.
- Has some optimization on the Jasmin code generation.
- Implements extra semantic analizers to check every single edge case.

## Cons:

- Dispite being fast in general there is some room of improvement in terms of performance in semantic analysis.
- Does not use Ollir to generate intermediate code.
- Does not have register level optimizations as it does not use Ollir.

## Project Setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.
