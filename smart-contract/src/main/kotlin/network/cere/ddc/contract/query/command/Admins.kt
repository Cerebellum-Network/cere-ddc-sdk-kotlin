package network.cere.ddc.contract.query.command

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface Admins {

    suspend fun adminGet(): AccountId
    suspend fun adminChange(newAdmin: AccountId, predictGasLimit: Boolean = false)
    suspend fun adminWithdraw(amount: Balance, predictGasLimit: Boolean = false)

}