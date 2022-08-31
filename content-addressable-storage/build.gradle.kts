dependencies {
    implementation(project(":core"))
    implementation(project(":proto"))

    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")

    // CID
    implementation("com.github.ipld:java-cid:${Versions.cid}")
}
