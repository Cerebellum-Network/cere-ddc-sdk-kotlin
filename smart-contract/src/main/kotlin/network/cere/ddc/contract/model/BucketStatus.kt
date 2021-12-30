package network.cere.ddc.contract.model

import java.time.LocalDateTime

data class BucketStatus(
    val providerId: String,
    val rentEnd: LocalDateTime,
    val writers: List<String>
)
