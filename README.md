# Cere DDC SDK for JVM languages

[![Release](https://jitpack.io/v/cerebellum-network/cere-ddc-sdk-kotlin.svg)](https://jitpack.io/#cerebellum-network/cere-ddc-sdk-kotlin)

## Requirements

This library requires Java version 11 or higher and Kotlin version 1.6 or higher.

## Modules

- [Content addressable Storage](content-addressable-storage/README.md)
- [Key value Storage](key-value-storage/README.md)
- [File Storage](file-storage/README.md)
- [Smart Contract](smart-contract/README.md)

## Basic configuration

### Gradle

Add repository in your Gradle build script:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```