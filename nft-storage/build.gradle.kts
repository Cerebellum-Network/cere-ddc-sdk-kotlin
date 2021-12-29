dependencies {
    implementation(project(":core"))

    testImplementation("io.ktor:ktor-client-mock:${Versions.ktor}")
    testImplementation("io.ktor:ktor-client-java:${Versions.ktor}")
}