package network.cere.ddc.contract

//TODo make dynamic reading hash by function name from JSON file
data class BucketContractConfig(
    //Bucket
    val bucketGetHash: String = "3802cb77",
    val bucketCreateHash: String = "0aeb2379",
    val bucketListHash: String = "417ab584",
    val bucketAllocIntoClusterHash: String = "4c482d19",
    val bucketSettlePaymentHash: String = "15974555",

    //Cluster
    val clusterCreateHash: String = "4c0f21f6",
    val clusterReserveResourceHash: String = "b5e38125",
    val clusterReplaceNodeHash: String = "48194ab1",
    val clusterGetHash: String = "e75411f5",
    val clusterListHash: String = "d9db9d44",
    val clusterDistributeRevenuesHash: String = "e71e66fc",

    //Node
    val nodeCreateHash: String = "b77ac1bb",
    val nodeGetHash: String = "847f3997",
    val nodeListHash: String = "423286d6"
)
