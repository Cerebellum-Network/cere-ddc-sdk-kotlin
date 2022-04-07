package network.cere.ddc.contract.blockchain.model

import network.cere.ddc.contract.model.AccountId

data class ContractCallEvent(
    val executor: AccountId,
    val data: ByteArray
)
