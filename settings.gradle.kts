rootProject.name = "DependencyChecker"

include("DependencyCheckerTests")
include("DependencyCheckerTests:ModuleA", "DependencyCheckerTests:ModuleB")