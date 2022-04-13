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
    val nodeListHash: String = "423286d6",

    //Admin
    val adminGetHash: String = "1261dae1",
    val adminChangeHash: String = "12292787",
    val adminWithdrawHash: String = "2f6e0868",

    //Billing
    val accountDepositHash: String = "c311af62",
    val accountGetHash: String = "1d4220fa",

    //Permission
    val permTrustHash: String = "32e7de43",
    val permHasTrustHash: String = "c50c38f7",
)
