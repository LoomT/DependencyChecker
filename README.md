## About DependencyChecker

Terminal application that checks if the JAR files contain all required dependencies to execute the specified main class.
Assumes there is no binary incompatibilities, it only checks if referenced classes exist in any of the JAR files.

### Running from command line

Build the application: `./gradlew installDist`, the executables will appear in build/install/DependencyChecker/bin directory

Usage: `DependencyChecker <main-class> [<jar-path>]+`

\+ means 1 or more times

\<main-class>: name of main class

\<jar-path>: path to jar file

#### Example

`./DependencyChecker "com.name.class" "moduleA" "moduleB" "subfolder/moduleC"`