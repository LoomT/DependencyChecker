package loomt;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DependencyChecker {

    public boolean checkDependencies(String mainClassNameWithDots, List<String> jarPaths) throws Exception {
        Set<String> availableClasses = new HashSet<>();
        Set<String> referencedClasses = new HashSet<>();

        List<File> jarFiles = new ArrayList<>();
        // collect all classes from the JAR files
        for (String jarPath : jarPaths) {
            File jarFile = new File(jarPath);
            if (!jarFile.exists() || !jarFile.isFile()) {
                throw new IOException("JAR file not found: " + jarPath);
            }
            jarFiles.add(jarFile);
            availableClasses.addAll(getClassesFromJar(jarFile));
        }

        Stack<String> classesToCheck = new Stack<>();
        classesToCheck.add(mainClassNameWithDots);

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
            if(!classFound) {
                System.err.println("Class not found: " + className);
                return false;
            }
        }

        // check if all referenced classes are available
        for (String refClass : referencedClasses) {
            if (!availableClasses.contains(refClass)) {
                System.err.println("Missing dependency: " + refClass);
                return false;
            }
        }

        return true;
    }

    /**
     * Get all available class names from a JAR file
     * @param jarFile jar file
     * @return set of class names
     * @throws IOException if an I/O error has occurred
     */
    private Set<String> getClassesFromJar(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            return jar.stream()
                    .filter(e -> e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace(".class", "").replace("/", "."))
                    .collect(Collectors.toSet());
        }
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
            System.out.println(method.name);
            for (AbstractInsnNode insn : method.instructions) {
                String classNameReferenced;
                System.out.println(insn.getOpcode());

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

                if(isStandardLibraryClass(classNameReferenced))
                    continue;

                classes.add(classNameReferenced);
            }
            for (LocalVariableNode localVariable : method.localVariables) {
                classes.addAll(splitSignature(localVariable.desc));
                if(localVariable.signature != null)
                    classes.addAll(splitSignature(localVariable.signature));
            }
        }

        for (FieldNode field : classNode.fields) {
            classes.addAll(splitSignature(field.desc));
            if(field.signature != null)
                classes.addAll(splitSignature(field.signature));
        }

        return classes;
    }

    // split complex templates hopefully
    private List<String> splitSignature(String signature) {
        return Arrays.stream(signature.replace("[", "").split("[;<>]"))
                .filter(t -> t.startsWith("L")) // filter for objects only
                .map(t -> t.substring(1))
                .map(t -> t.replace("/", "."))
                .filter(t -> !isStandardLibraryClass(t))
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
    private boolean isStandardLibraryClass(String className) {
        return className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("jdk.") ||
                className.startsWith("sun.") ||    // classes from the sun package
                className.startsWith("com.sun.");  // classes from com.sun package
    }
}
