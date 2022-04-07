package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface AdminCommander {

    fun adminGet(): AccountId
    fun adminChange(newAdmin: AccountId)
    fun adminWithdraw(amount: Balance)

}