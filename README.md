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
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:core:0.2.0.Final")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:object-storage:0.2.0.Final")
}
```

### Object Storage

#### Create client

```kotlin
val trustedNodes = listOf(
    Node(id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT", address = "https://127.0.0.1:8080"),
    Node(id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC", address = "https://127.0.0.2:8080")
)
val storage: ObjectStorage = ObjectStorageBuilder().trustedNodes(trustedNodes).privateKey(privateKeyHex).build()
```

#### Store

```kotlin
val bucketId = 10L

// Object
val objectPath: ObjectPath = storage.storeObject(bucketId, encryptedObjectBytes)

// EDEK
val edek = Edek(publicKeyHex, value)
val storedEdek: Edek = storage.storeEdek(objectPath, edek)
```

#### Read

```kotlin
val objectBytes: ByteArray = storage.readObject(objectPath)
val edek: Edek = storage.readEdek(objectPath, publicKeyHex)
```