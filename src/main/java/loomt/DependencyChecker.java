package loomt;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyChecker {

    /**
     * @param mainClassName name of main class
     * @param jarPaths paths to jar files
     * @return string optional of missing class name or empty otherwise
     * @throws IOException if an I/O error has occurred while reading jar files
     */
    public Optional<String> checkDependencies(String mainClassName, List<String> jarPaths) throws IOException {
        Set<String> referencedClasses = new HashSet<>();

        List<File> jarFiles = new ArrayList<>();
        // collect all classes from the JAR files
        for (String jarPath : jarPaths) {
            File jarFile = new File(jarPath);
            if (!jarFile.exists() || !jarFile.isFile()) {
                throw new IOException("JAR file not found: " + jarPath);
            }
            jarFiles.add(jarFile);
        }

        Stack<String> classesToCheck = new Stack<>();
        classesToCheck.add(mainClassName);
        referencedClasses.add(mainClassName);

        while (!classesToCheck.isEmpty()) {
            String className = classesToCheck.pop();
            boolean classFound = false;
            // analyze the class and collect all referenced classes
            for (File jarFile : jarFiles) {
                ClassNode classNode = getClassNodeFromJar(jarFile, className);
                if (classNode != null) {
                    Set<String> classes = getReferencedClassesFromClass(classNode);
                    classesToCheck.addAll(classes.stream()
                            .filter(c -> !referencedClasses.contains(c))
                            .collect(Collectors.toSet()));
                    referencedClasses.addAll(classes);
                    classFound = true;
                    break;
                }
            }
            if (!classFound) {
                return Optional.of(className);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the ClassNode (ASM representation) from a JAR file for a specific class
     * @param jarFile jar file to search in
     * @param className class name
     * @return ClassNode iff class is found, null otherwise
     * @throws IOException if an I/O error has occurred
     */
    private ClassNode getClassNodeFromJar(File jarFile, String className) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry(className.replace('.', '/') + ".class");
            if (entry != null) {
                ClassReader reader = new ClassReader(jar.getInputStream(entry));
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);
                return classNode;
            }
        }
        return null;
    }

    /**
     * Get all referenced classes from a given class file using ASM,
     * including static method calls, and ignore standard library classes
     * @param classNode class to examine
     * @return set of referenced classes
     */
    private Set<String> getReferencedClassesFromClass(ClassNode classNode) {
        Set<String> classes = new HashSet<>();
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                String classNameReferenced;

                // check for instantiation, cast, or instanceof operations
                if (insn.getOpcode() == Opcodes.NEW || insn.getOpcode() == Opcodes.ANEWARRAY
                        || insn.getOpcode() == Opcodes.MULTIANEWARRAY
                        || insn.getOpcode() == Opcodes.CHECKCAST
                        || insn.getOpcode() == Opcodes.INSTANCEOF) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    // convert from internal format to class name format
                    classNameReferenced = typeInsn.desc.replace('/', '.')
                            .replace("[", ""); // strip array symbols
                    if(classNameReferenced.startsWith("L") && classNameReferenced.endsWith(";")) {
                        // remove L prefix and ; suffix
                        classNameReferenced = classNameReferenced.substring(1, classNameReferenced.length() - 1);
                    }
                    if(isPrimitive(classNameReferenced)) continue;
                }

                // check for static method calls
                // and constructor calls in case of extending a missing class
                else if (insn.getOpcode() == Opcodes.INVOKESTATIC || insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    classNameReferenced = methodInsn.owner.replace('/', '.');
                }

                // check for static field accesses
                else if (insn.getOpcode() == Opcodes.GETSTATIC || insn.getOpcode() == Opcodes.PUTSTATIC) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    classNameReferenced = fieldInsn.owner.replace('/', '.');
                }
                else continue;

                classes.add(classNameReferenced);
            }
            // add local variable signatures because generic classes only show their assigned type here
            // if the generic class is never assigned, the bytecode does not have the assigned type it seems?
            for (LocalVariableNode localVariable : method.localVariables) {
                classes.addAll(splitSignature(localVariable.desc));
                if(localVariable.signature != null)
                    classes.addAll(splitSignature(localVariable.signature));
            }
            // add method return type
            classes.addAll(splitSignature(method.desc.substring(method.desc.indexOf(")") + 1)));

            // extract method, its parameters and local variables annotations
            classes.addAll(getAnnotations(method));
        }

        for (FieldNode field : classNode.fields) {
            classes.addAll(splitSignature(field.desc));
            if(field.signature != null)
                classes.addAll(splitSignature(field.signature));
            classes.addAll(getAnnotations(field));
        }

        // add classes annotations
        classes.addAll(getAnnotations(classNode));

        // filter out std classes
        return classes.stream().filter(this::isNotStandardLibraryClass).collect(Collectors.toSet());
    }

    private Set<String> getAnnotations(MethodNode method) {
        List<AnnotationNode> annotations =
                getBasicAnnotations(method.visibleAnnotations, method.invisibleAnnotations,
                        method.visibleTypeAnnotations, method.invisibleTypeAnnotations);
        if (method.visibleParameterAnnotations != null)
            for (List<AnnotationNode> parameterAnnotations : method.visibleParameterAnnotations)
                if(parameterAnnotations != null)
                    annotations.addAll(parameterAnnotations);
        if (method.invisibleParameterAnnotations != null)
            for (List<AnnotationNode> parameterAnnotations : method.invisibleParameterAnnotations)
                if(parameterAnnotations != null)
                    annotations.addAll(parameterAnnotations);
        if (method.visibleLocalVariableAnnotations != null)
            annotations.addAll(method.visibleLocalVariableAnnotations);
        if (method.invisibleLocalVariableAnnotations != null)
            annotations.addAll(method.invisibleLocalVariableAnnotations);
        return annotations.stream()
                .map(a -> a.desc.substring(1, a.desc.length() - 1).replace("/", "."))
                .collect(Collectors.toSet());
    }

    private Set<String> getAnnotations(FieldNode field) {
        List<AnnotationNode> annotations =
                getBasicAnnotations(field.visibleAnnotations, field.invisibleAnnotations,
                        field.visibleTypeAnnotations, field.invisibleTypeAnnotations);
        return annotations.stream()
                .map(a -> a.desc.substring(1, a.desc.length() - 1).replace("/", "."))
                .collect(Collectors.toSet());
    }

    private Set<String> getAnnotations(ClassNode classNode) {
        List<AnnotationNode> annotations =
                getBasicAnnotations(classNode.visibleAnnotations, classNode.invisibleAnnotations,
                        classNode.visibleTypeAnnotations, classNode.invisibleTypeAnnotations);
        return annotations.stream()
                .map(a -> a.desc.substring(1, a.desc.length() - 1).replace("/", "."))
                .collect(Collectors.toSet());
    }

    private List<AnnotationNode> getBasicAnnotations(List<AnnotationNode> visibleAnnotations, List<AnnotationNode> invisibleAnnotations, List<TypeAnnotationNode> visibleTypeAnnotations, List<TypeAnnotationNode> invisibleTypeAnnotations) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (visibleAnnotations != null)
            annotations.addAll(visibleAnnotations);
        if (invisibleAnnotations != null)
            annotations.addAll(invisibleAnnotations);
        if (visibleTypeAnnotations != null)
            annotations.addAll(visibleTypeAnnotations);
        if (invisibleTypeAnnotations != null)
            annotations.addAll(invisibleTypeAnnotations);
        return annotations;
    }

    /**
     * Hopefully splits complex generic class types
     * @param signature class type (not a function signature)
     * @return list of non std classes
     */
    private List<String> splitSignature(String signature) {
        return Arrays.stream(signature.replace("[", "").split("[;<>]"))
                .filter(t -> t.startsWith("L")) // filter for objects only
                .map(t -> t.substring(1))
                .map(t -> t.replace("/", "."))
                .toList();
    }

    /**
     * @param classNameReferenced class to examine
     * @return true iff class name is a primitive type
     */
    private boolean isPrimitive(String classNameReferenced) {
        return switch (classNameReferenced) {
            case "V", "Z", "B", "C", "S", "I", "J", "F", "D" -> true;
            default -> false;
        };
    }

    /**
     * Helper method to check if the class belongs to the Java standard library
     * @param className class name to check
     * @return true iff the class belongs to the standard library
     */
    private boolean isNotStandardLibraryClass(String className) {
        return Stream.of("java.", "javax.", "jdk.", "sun.", "com.sun.").noneMatch(className::startsWith);
    }
}
