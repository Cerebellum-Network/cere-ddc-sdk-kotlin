plugins {
    kotlin("jvm") version "1.5.10"

    maven
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
}