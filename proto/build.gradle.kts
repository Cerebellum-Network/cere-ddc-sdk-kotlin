import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf") version Versions.protobufPlugin
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
}
