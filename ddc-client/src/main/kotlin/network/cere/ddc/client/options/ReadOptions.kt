package network.cere.ddc.client.options

data class ReadOptions(
    // dekPath - Used to calculate DEK (bucketId + dekPath + client public key). Empty if not passed.
    val dekPath: String?,
    // decrypt - If not passed, check 'cipher' parameter (decrypt if cipher configured)
    val decrypt: Boolean
)
