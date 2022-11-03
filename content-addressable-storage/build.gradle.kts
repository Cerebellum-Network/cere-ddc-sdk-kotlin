dependencies {
    implementation(project(":core"))
    implementation(project(":proto"))

    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
    implementation("io.ktor:ktor-client-okhttp:${Versions.ktor}")
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
    implementation("com.github.komputing.khex:extensions:${Versions.khex}")

    // CID
    implementation("com.github.ipld:java-cid:${Versions.cid}")
}
