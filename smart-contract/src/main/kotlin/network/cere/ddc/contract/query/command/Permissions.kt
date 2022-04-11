package network.cere.ddc.contract.query.command

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface Permissions {

    suspend fun permTrust(value: Balance, trustee: AccountId)
    suspend fun permHasTrust(trustee: AccountId, trustGiver: AccountId): Boolean
}