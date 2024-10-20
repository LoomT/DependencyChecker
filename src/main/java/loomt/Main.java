package loomt;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java DependencyChecker <main-class> <jar-paths>");
            return;
        }

        String mainClassName = args[0]; // Convert to path format
        List<String> jarPaths = List.of(args).subList(1, args.length);
        DependencyChecker checker = new DependencyChecker();
        try {
            if (checker.checkDependencies(mainClassName, jarPaths)) {
                System.out.println("All required dependencies are satisfied.");
            } else {
                System.out.println("Some dependencies are missing.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
