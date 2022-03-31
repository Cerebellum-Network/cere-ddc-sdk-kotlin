package network.cere.ddc.contract.model

import java.time.LocalDateTime

data class DealStatus(
    val nodeId: Long,
    val extimatedRentEnd: LocalDateTime //u64
)
