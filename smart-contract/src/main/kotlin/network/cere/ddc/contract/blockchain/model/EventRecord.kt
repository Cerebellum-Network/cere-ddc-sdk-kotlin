package network.cere.ddc.contract.blockchain.model

import io.emeraldpay.polkaj.types.Hash256

data class EventRecord<T>(
    val phase: Phase,
    val id: Long,
    val moduleId: Int,
    val eventId: Int,
    val event: T?,
    val topics: List<Hash256>
) {

    enum class Phase {
        APPLY_EXTRINSIC, FINALISATION, INITIALIZATION
    }
}