package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.Account
import network.cere.ddc.contract.model.AccountId

interface BillingCommander {
    //payable
    fun accountDeposit()
    //result
    fun accountGet(accountId: AccountId): Account
}