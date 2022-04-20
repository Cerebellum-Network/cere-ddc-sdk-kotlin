# File Storage

## Usage

### Gradle

```groovy
dependencies {
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:content-addressable-storage:1.1.0.Prototype")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:file-storage:1.1.0.Prototype")
}
```

## Example

### Setup

```kotlin
val scheme = Scheme.create(Scheme.SR_25519, privateKey)
val gatewayNodeUrl = "http://localhost:8080"
val storage = FileStorage(scheme, gatewayNodeUrl)
```

### Store file

```kotlin
val path = Paths.get("resources", "file.txt")
val bucketId = 1L

val headPieceUri: PieceUri = storage.upload(bucketId, path)
```

### Download file

```kotlin
val newFilePath = Paths.get("resources", "newFile.txt")

storage.download(bucketId, headPieceUri.cid, newFilePath)
```

### Read file

```kotlin
val fileData: ByteArray = storage.read(bucketId, headPieceUri.cid)
```