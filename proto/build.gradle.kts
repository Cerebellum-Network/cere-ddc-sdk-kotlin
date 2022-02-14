plugins {
    id("com.google.protobuf") version Versions.protobufPlugin
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
}
