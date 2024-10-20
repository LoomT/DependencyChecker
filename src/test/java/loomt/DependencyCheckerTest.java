package loomt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyCheckerTest {
    private DependencyChecker checker;
    private final String jarPath = "build/resources/test/";

    @BeforeEach
    void setUp() {
        checker = new DependencyChecker();
    }


    @Test
    void simpleMissingDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void simpleNotMissingDependency() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void noDependencies() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassA",
                List.of(jarPath + "ModuleA-1.0.jar")));
    }

    @Test
    void twoDepthMissingLastDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.SomeAnotherClass",
                List.of(jarPath + "ModuleA-1.0.jar")));
    }

    @Test
    void twoDepth() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.SomeAnotherClass",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "commons-io-2.16.1.jar")));
    }

    @Test
    void loop() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB1",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void fieldInitNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB3",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void fieldInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB3",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void fieldNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB4",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void field() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB4",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void arrayInitNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void array2dInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray2d",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void arrayInit2dNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray2d",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void arrayInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void childClass() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassAChild",
                List.of(jarPath + "ModuleA-1.0.jar")));
    }

    @Test
    void childExtendsNoParentDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBChild",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void childExtends() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBChild",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void templateNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBTemplate",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void template() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBTemplate",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void templateComplexNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBComplexTemplate",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void templateComplex() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBComplexTemplate",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void exceptionThrowNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBThrow",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void exceptionThrow() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBThrow",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void argumentNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArgument",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void argument() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArgument",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void returnANoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBReturnA",
                List.of(jarPath + "ModuleB-1.0.jar")));
    }

    @Test
    void returnA() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBReturnA",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")));
    }
}