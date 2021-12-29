package network.cere.ddc.contract.config

data class ObjectStorageSmartContractConfig(
    val address: String,
    val getRentEndMsFunctionName: String,
    val topUpBucketFunctionName: String,
    val createBucketFunctionName: String,
)