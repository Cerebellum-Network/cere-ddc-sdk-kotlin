# Smart Contract

## Usage

### Gradle

build.gradle.kts

```groovy
repositories {
    maven("https://jitpack.io")
    ivy("https://github.com") {
        patternLayout {
            artifact("[organisation]/releases/download/v[revision]/[module]-[revision].jar")
            setM2compatible(true)
        }
        metadataSources { artifact() }
    }
}

dependencies {
    api("com.github.Cerebellum-Network.cere-ddc-sdk-kotlin:smart-contract:1.1.0.Prototype")
}
```

## Example

```kotlin
val config = ContractConfig(
    wsUrl = "wss://rpc.testnet.cere.network:9945",
    contractAddressHex = "5HVvafs4xBbSnVDFSam9tArJrAFX8xnZALUn3cgDS5RDBufB",
    privateKeyHex = "0xa8askldaf21093..."
)
val contractConfig = BucketContractConfig()
val smartContract = BucketSmartContract.buildAndConnect(config, contractConfig)

val bucketStatus: BucketStatus = smartContract.bucketGet(0L)
```