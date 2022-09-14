package network.cere.ddc.client.options

data class ReadOptions(
    // dekPath - Used to calculate DEK (bucketId + dekPath + client public key). Empty if not passed.
    val dekPath: String?,
    // encrypt - If not passed, check 'cipher' parameter (encrypt if cipher configured)
    val encrypt: Boolean?
)
