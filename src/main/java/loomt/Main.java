package loomt;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("Usage: DependencyChecker <main-class> [<jar-path>]+");
            System.out.println("Example: ./DependencyChecker \"com.name.class\" \"moduleA\" \"moduleB\" \"moduleC\"");
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
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
