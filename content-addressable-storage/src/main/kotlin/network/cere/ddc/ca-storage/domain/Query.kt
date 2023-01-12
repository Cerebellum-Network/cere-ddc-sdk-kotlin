package network.cere.ddc.`ca-storage`.domain

data class Query(val bucketId: Long, val tags: List<Tag>, val skipData: Boolean = false)
