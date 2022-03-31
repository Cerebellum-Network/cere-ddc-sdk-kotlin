package network.cere.ddc.contract.model

data class Bucket(
    val ownerId: AccountId,
    val clusterIds: List<Long>,
    val dealIds: List<Long>,
    val bucketParams: String
)
