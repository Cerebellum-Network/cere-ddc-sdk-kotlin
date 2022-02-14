# Cere DDC SDK for JVM languages

[![Release](https://jitpack.io/v/cerebellum-network/cere-ddc-sdk-kotlin.svg)](https://jitpack.io/#cerebellum-network/cere-ddc-sdk-kotlin)

## Requirements

This library requires Java version 11 or higher and Kotlin version 1.6 or higher.

## Usage

### Gradle

Add dependency in your Gradle build script:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("com.github.cerebellum-network.cere-ddc-sdk-kotlin:content-addressable-storage:1.0.0.Prototype")
    implementation("com.github.cerebellum-network.cere-ddc-sdk-kotlin:key-value-storage:1.0.0.Prototype")
}
```

### Content addressable storage

#### Setup

```kotlin
val scheme = Scheme.create(Scheme.SR_25519, privateKey)
val gatewayNodeUrl = "http://localhost:8080"
val storage = ContentAddressableStorage(scheme, gatewayNodeUrl)
```

#### Store

```kotlin
val bucketId = 1L
val piece = Piece(
    data = "Hello world!".toByteArray(),
    tags = listOf(Tag(key = "Creator", value = "Jack"))
)

// EDEK
val pieceUrl = storage.store(bucketId, piece)
```

#### Read

```kotlin
val savedPiece = storage.read(bucketId, pieceUrl.cid)
```

### Key value storage

#### Setup

```kotlin
val scheme = Scheme.create(Scheme.SR_25519, privateKey)
val gatewayNodeUrl = "http://localhost:8080"
val storage = KeyValueStorage(scheme, gatewayNodeUrl)
```

#### Store

```kotlin
val bucketId = 1L
val key = "unique piece"
val piece = Piece(
    data = "Hello world!".toByteArray(),
    tags = listOf(Tag(key = "Creator", value = "Jack"))
)

// EDEK
val pieceUrl = storage.store(bucketId, key, piece)
```

#### Read

```kotlin
val savedPiece = storage.read(bucketId, key)
```
