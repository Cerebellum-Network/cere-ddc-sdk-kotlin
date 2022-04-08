package network.cere.ddc.contract.query.service

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.query.commander.PermissionCommander

class PermissionService(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    PermissionCommander {

    override suspend fun permTrust(value: Balance, trustee: AccountId) {
        client.callTransaction(contractConfig.permTrustHash) {
            write(BalanceScale, value)
            write(AccountIdScale, trustee)
        }
    }

    override suspend fun permHasTrust(trustee: AccountId, trustGiver: AccountId): Boolean {
        val result = client.call(contractConfig.permHasTrustHash) {
            write(AccountIdScale, trustee)
            write(AccountIdScale, trustGiver)
        }

        return result.readBoolean()
    }
}