package network.cere.ddc.contract.query.command

import network.cere.ddc.contract.model.Balance

interface Admins {
    suspend fun adminWithdraw(amount: Balance, predictGasLimit: Boolean = false)

}