Playwright for Java
======================

This library is dedicated for testing projects utilizing Microsoft's Playwright for Java automation tool. It provides easy
way to manage Playwright resources and provides a set of utilities to make writing tests easier.

# Tools, libraries, and technologies

## Lombok

This project uses lombok to decrease boilerplate code and to allow these generated methods to be
excluded from code coverage checks. If you are using Intellij please install the Lombok Plugin. If
you are using Eclipse STS follow the instructions [here](https://projectlombok.org/setup/eclipse).
If you are using another IDE you can see if it is supported on the Lombok website [here](https://projectlombok.org).

## Code Quality

As part of the build, there are several code quality checks running against the code base. All code quality files can be
found in the root of the project under the [codequality](../playwright-manager/.codequality) directory.

### CheckStyle

The project runs checkstyle plugin to validate java code formatting and enforce best coding standards.

### PMD

The project runs PMD code analysis to find common programming flaws like unused variables, empty catch blocks, unnecessary
object creation, and etc...