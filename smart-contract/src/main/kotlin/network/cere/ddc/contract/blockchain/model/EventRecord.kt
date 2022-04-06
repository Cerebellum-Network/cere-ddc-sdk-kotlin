package network.cere.ddc.contract.blockchain.model

import io.emeraldpay.polkaj.types.Hash256

data class EventRecord(
    val phase: Phase,
    val id: Long,
    val type: String,
    val attributes: ByteArray,
    val topics: List<Hash256>
) {
    enum class Phase {
        APPLY_EXTRINSIC, FINALISATION, INITIALIZATION
    }
}