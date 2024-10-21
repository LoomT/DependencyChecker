plugins {
    id("java")
}

group = "loomt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.ow2.asm:asm-tree:9.7.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":DependencyCheckerTests:ModuleA"))
    testImplementation(project(":DependencyCheckerTests:ModuleB"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":DependencyCheckerTests:ModuleA:jar", ":DependencyCheckerTests:ModuleB:jar")
    dependsOn(":processTestResources")
}

//tasks.named<ProcessResources>("processTestResources") {
//    val jarFiles = (tasks.getByPath(":DependencyCheckerTests:ModuleA:jar").outputs.files)
//        .plus(tasks.getByPath(":DependencyCheckerTests:ModuleB:jar").outputs.files)
//    println(jarFiles.forEach { t -> t.toString() })
//    inputs.files(jarFiles)
//    from(jarFiles) {
//        into("$rootDir/build/resources/testJars/")
//    }
//}