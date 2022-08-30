dependencies {
    implementation(project(":core"))
    implementation(project(":proto"))

    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
    implementation("org.apache.tuweni:tuweni-crypto:${Versions.tuweni}")
    implementation("org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}")

    // CID
    implementation("com.github.ipld:java-cid:${Versions.cid}")
}
