package network.cere.ddc.contract.options

class BucketParams(replication: Int?) {
    private val replication: Int

    //move to properties?
    private companion object {
        const val DEFAULT_REPLICATION: Int = 1
        const val MAX_REPLICATION = 3
    }

    init {
        this.replication = when {
            replication == null -> DEFAULT_REPLICATION
            replication < 0 -> throw Exception("Exceed bucket limits: $replication")
            replication > MAX_REPLICATION -> throw Exception("Invalid bucket params: $replication")
            else -> replication
        }
    }
}



