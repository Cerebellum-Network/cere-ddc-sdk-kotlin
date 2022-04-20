# Key value Storage

## Usage

### Gradle

```groovy
dependencies {
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:content-addressable-storage:1.0.0.Prototype")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:key-value-storage:1.0.0.Prototype")
}
```

## Example

### Setup

```kotlin
val scheme = Scheme.create(Scheme.SR_25519, privateKey)
val gatewayNodeUrl = "http://localhost:8080"
val storage = KeyValueStorage(scheme, gatewayNodeUrl)
```

### Store

```kotlin
val bucketId = 1L
val key = "unique piece"
val piece = Piece(
    data = "Hello world!".toByteArray(),
    tags = listOf(Tag(key = "Creator", value = "Jack"))
)

val pieceUrl = storage.store(bucketId, key, piece)
```

### Read

```kotlin
val savedPieces = storage.read(bucketId, key)
```