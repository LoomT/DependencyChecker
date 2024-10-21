group = "com.jetbrains.internship2024"
version = "1.0"

subprojects {
    apply(plugin = "java")
    tasks.register("copy_io") {
        val compileClasspath =
            project.configurations.matching { it.name == "compileClasspath" }
        compileClasspath.all {
            for (dep in map { file: File -> file.absoluteFile }) {
                project.copy {
                    from(dep)
                    into("${rootProject.projectDir}/build/resources/test")
                }
            }
        }
    }
    tasks.withType<Jar>() {
        destinationDirectory = file("$rootDir/build/resources/test")
        dependsOn("copy_io")
    }
}