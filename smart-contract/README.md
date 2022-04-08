# Smart Contract

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