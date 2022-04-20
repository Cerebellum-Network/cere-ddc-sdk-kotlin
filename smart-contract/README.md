# Smart Contract

## Usage

### Gradle

build.gradle.kts

```kotlin
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

### Setup

```kotlin
val config = ContractConfig(
    wsUrl = "wss://rpc.testnet.cere.network:9945",
    contractAddressHex = "5HVvafs4xBbSnVDFSam9tArJrAFX8xnZALUn3cgDS5RDBufB",
    privateKeyHex = "0xa8askldaf21093..."
)
val contractConfig = BucketContractConfig()
val smartContract = BucketSmartContract.buildAndConnect(config, contractConfig)
```

### Create bucket

```kotlin
//10 CERE tokens
val value = Balance(BigDecimal("10"))
    
val bucketEvent: BucketCreatedEvent = smartContract.bucketCreate(value, "{\"replication\": 3}", clusterId)
val bucketStatus: BucketStatus = smartContract.bucketGet(bucketEvent.bucketId)
```