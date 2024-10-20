package loomt;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.HashSet;
import java.util.Set;

public record DepTypes(Set<TypeInsnNode> types, Set<MethodInsnNode> methods, Set<FieldInsnNode> fields) {}
