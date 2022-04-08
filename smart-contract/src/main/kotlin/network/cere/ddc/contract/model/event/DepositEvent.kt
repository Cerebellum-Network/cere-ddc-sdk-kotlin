package network.cere.ddc.contract.model.event

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

data class DepositEvent(
    val accountId: AccountId,
    val value: Balance
)