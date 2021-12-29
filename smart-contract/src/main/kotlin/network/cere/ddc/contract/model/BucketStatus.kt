package network.cere.ddc.contract.model

import java.time.ZonedDateTime

data class BucketStatus(
    val providerId: String,
    val rentEnd: ZonedDateTime,
    val writers: List<String>
)
