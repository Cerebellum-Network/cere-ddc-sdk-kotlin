# Smart Contract

## Example

```kotlin
val config = ContractConfig(
    wsUrl = "wss://rpc.testnet.cere.network:9944",
    contractAddressHex = "5D1fsUPyamMquGDZxSKj3E1YmG3QWp6tMHPktP5DPas3pccr",
    privateKeyHex = "0xa8askldaf21093..."
)
val contractConfig = BucketContractConfig()
val smartContract = BucketSmartContract.buildAndConnect(config, contractConfig)

val bucketStatus: BucketStatus = smartContract.bucketGet(0L)
```