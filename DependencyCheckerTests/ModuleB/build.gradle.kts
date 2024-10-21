plugins {
    id("java")
}

group = "com.jetbrains.internship2024"
version = "1.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(project(":DependencyCheckerTests:ModuleA"))
}
