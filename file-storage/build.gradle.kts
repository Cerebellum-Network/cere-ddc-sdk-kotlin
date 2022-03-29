dependencies {
    implementation(project(":core"))
    implementation(project(":proto"))
    implementation(project(":content-addressable-storage"))

    // CID
    implementation("com.github.ipld:java-cid:${Versions.cid}")
}
