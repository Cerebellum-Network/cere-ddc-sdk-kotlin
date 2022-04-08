package network.cere.ddc.contract.query.service

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.query.commander.AdminCommander

class AdminService(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    AdminCommander {

    override suspend fun adminGet(): AccountId {
        val response = client.call(contractConfig.adminGetHash)

        return response.read(AccountIdScale)
    }

    override suspend fun adminChange(newAdmin: AccountId) {
        client.callTransaction(contractConfig.adminChangeHash) {
            write(AccountIdScale, newAdmin)
        }
    }

    override suspend fun adminWithdraw(amount: Balance) {
        client.callTransaction(contractConfig.adminWithdrawHash) {
            write(BalanceScale, amount)
        }
    }
}