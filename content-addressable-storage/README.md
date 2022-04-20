# Content addressable storage

## Usage

### Gradle

```groovy
dependencies {
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:content-addressable-storage:1.1.0.Prototype")
}
```

### Setup

```kotlin
val scheme = Scheme.create(Scheme.SR_25519, privateKey)
val gatewayNodeUrl = "http://localhost:8080"
val storage = ContentAddressableStorage(scheme, gatewayNodeUrl)
```

### Store

```kotlin
val bucketId = 1L
val piece = Piece(
    data = "Hello world!".toByteArray(),
    tags = listOf(Tag(key = "Creator", value = "Jack"))
)

val pieceUrl = storage.store(bucketId, piece)
```

### Read

```kotlin
val savedPiece = storage.read(bucketId, pieceUrl.cid)
```