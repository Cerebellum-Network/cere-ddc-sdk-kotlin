dependencies {
    implementation(project(":core"))

    testImplementation("io.ktor:ktor-client-mock:${Versions.ktor}")
    testImplementation("io.ktor:ktor-client-java:${Versions.ktor}")
}

dockerCompose {
    isRequiredBy(tasks.test)
    useComposeFiles = listOf("${ rootProject.buildDir }/../docker-compose/docker-compose.yml")
}