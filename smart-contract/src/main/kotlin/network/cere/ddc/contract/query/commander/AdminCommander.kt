package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface AdminCommander {

    suspend fun adminGet(): AccountId
    suspend fun adminChange(newAdmin: AccountId)
    suspend fun adminWithdraw(amount: Balance)

}