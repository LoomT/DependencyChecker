package loomt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
    void mainClassNotFound() throws IOException {
        assertEquals("com.jetbrains.internship2024.OnePiece",
                checker.checkDependencies("com.jetbrains.internship2024.OnePiece",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).get());
    }

    @Test
    void fileNotFound() {
        assertThrows(IOException.class,
                () -> checker.checkDependencies("com.jetbrains.internship2024.ClassA",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleC-1.0.jar")));
    }

    @Test
    void simpleMissingDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void simpleNotMissingDependency() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void noDependencies() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassA",
                List.of(jarPath + "ModuleA-1.0.jar")).isEmpty());
    }

    @Test
    void twoDepthMissingLastDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.SomeAnotherClass",
                List.of(jarPath + "ModuleA-1.0.jar")).isEmpty());
    }

    @Test
    void twoDepth() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.SomeAnotherClass",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "commons-io-2.16.1.jar")).isEmpty());
    }

    @Test
    void loop() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB1",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void fieldInitNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB3",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void fieldInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB3",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void fieldNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassB4",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void field() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassB4",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void arrayInitNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void array2dInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray2d",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void arrayInit2dNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray2d",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void arrayInit() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArray",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void childClass() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassAChild",
                List.of(jarPath + "ModuleA-1.0.jar")).isEmpty());
    }

    @Test
    void childExtendsNoParentDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBChild",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void childExtends() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBChild",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void templateNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBTemplate",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void template() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBTemplate",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void templateComplexNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBComplexTemplate",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void templateComplex() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBComplexTemplate",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void exceptionThrowNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBThrow",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void exceptionThrow() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBThrow",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void argumentNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBArgument",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void argument() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBArgument",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void returnANoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ClassBReturnA",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void returnA() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ClassBReturnA",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void constructorAnnotationNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.annotations.Constructor",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void constructorAnnotation() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.annotations.Constructor",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void classAnnotationNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.annotations.Class",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void classAnnotation() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.annotations.Class",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void classTypeParameterAnnotationNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.annotations.ClassTypeParameter",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void classTypeParameterAnnotation() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.annotations.ClassTypeParameter",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void fieldAnnotationNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.annotations.Field",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void fieldAnnotation() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.annotations.Field",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void packageAnnotationNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.annotations.package-info",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void packageAnnotation() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.annotations.package-info",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void hasRecordANoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.record.HasRecordA",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void hasRecordA() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.record.HasRecordA",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void hasRecordBNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.record.HasRecordB",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void hasRecordB() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.record.HasRecordB",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void recordBNoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.record.RecordB",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void recordB() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.record.RecordB",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void InterfaceA() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.InterfaceA",
                List.of(jarPath + "ModuleA-1.0.jar")).isEmpty());
    }

    @Test
    void implementsANoDependency() throws Exception {
        assertFalse(checker.checkDependencies("com.jetbrains.internship2024.ImplementsA",
                List.of(jarPath + "ModuleB-1.0.jar")).isEmpty());
    }

    @Test
    void implementsA() throws Exception {
        assertTrue(checker.checkDependencies("com.jetbrains.internship2024.ImplementsA",
                List.of(jarPath + "ModuleA-1.0.jar", jarPath + "ModuleB-1.0.jar")).isEmpty());
    }
}