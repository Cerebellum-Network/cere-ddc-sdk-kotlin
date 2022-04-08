package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.Account
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.DepositEvent

interface BillingCommander {

    suspend fun accountDeposit(value: Balance): DepositEvent
    //result
    suspend fun accountGet(accountId: AccountId): Account
}