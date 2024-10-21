package loomt;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java DependencyChecker <main-class> <jar-paths>");
            return;
        }

        String mainClassName = args[0];
        List<String> jarPaths = List.of(args).subList(1, args.length);
        DependencyChecker checker = new DependencyChecker();
        try {
            Optional<String> missingClass = checker.checkDependencies(mainClassName, jarPaths);
            if (missingClass.isEmpty()) {
                System.out.println("All required dependencies are satisfied.");
            } else {
                System.out.println("Class not found: " + missingClass.get());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
