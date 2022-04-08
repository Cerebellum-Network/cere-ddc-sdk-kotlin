package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.DepositEvent
import network.cere.ddc.contract.model.response.Account

interface BillingCommander {

    suspend fun accountDeposit(value: Balance): DepositEvent
    suspend fun accountGet(accountId: AccountId): Account
}