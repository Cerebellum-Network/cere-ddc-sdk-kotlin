package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId

interface PermissionCommander {

    //payable
    fun permTrust(trustee: AccountId)
    fun permHasTrust(trustee: AccountId, trustGiver: AccountId): Boolean
}