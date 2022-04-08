package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.Account
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface BillingCommander {

    suspend fun accountDeposit(value: Balance)
    //result
    suspend fun accountGet(accountId: AccountId): Account
}