package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

interface PermissionCommander {

    suspend fun permTrust(value: Balance, trustee: AccountId)
    suspend fun permHasTrust(trustee: AccountId, trustGiver: AccountId): Boolean
}