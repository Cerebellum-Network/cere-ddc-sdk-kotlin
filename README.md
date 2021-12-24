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
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:core:0.0.1.Final")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:nft-storage:0.0.1.Final")
}
```

### NFT Storage

#### Create client

```kotlin
val trustedNodes = listOf(
    Node(id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT", address = "https://127.0.0.1:8080"),
    Node(id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC", address = "https://127.0.0.2:8080")
)
val storage = NftStorageBuilder().trustedNodes(trustedNodes).privateKey(privateKeyHex)
```

#### Store

```kotlin
val nftId = "someNftId"

// Asset
val assetNftPath: NftPath = storage.storeAsset(nftId, encryptedAssetBytes, "image.jpeg")

// Metadata ERC-721 schema
val metadata = Erc721Metadata(
    name = "Cere logo",
    description = "Cere Network official logo image",
    image = "https://cere.network/assets/images/home/footer-logo.svg"
)
val metadataNftPath: NftPath = storage.storeMetadata(nftId, metadata)

// EDEK
val edek = Edek(publicKeyHex, value)
val storedEdek: Edek = storage.storeEdek(nftId, metadataNftPath, edek)
```

#### Read

```kotlin
val assetBytes: ByteArray = storage.readAsset(nftId, assetNftPath)
val metadata: Metadata = storage.readMetadata(nftId, metadataNftPath)
val edek: Edek = storage.readEdek(nftId, metadataNftPath, publicKeyHex)
```