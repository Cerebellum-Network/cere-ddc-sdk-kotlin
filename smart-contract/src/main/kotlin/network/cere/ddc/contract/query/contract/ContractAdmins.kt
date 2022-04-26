package network.cere.ddc.contract.query.contract

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.query.command.Admins

class ContractAdmins(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    Admins {

    override suspend fun adminGet(): AccountId {
        val response = client.call(contractConfig.getMethodHashByName("admin_get"))

        return response.read(AccountIdScale)
    }

    override suspend fun adminChange(newAdmin: AccountId) {
        client.callTransaction(contractConfig.getMethodHashByName("admin_change")) {
            write(AccountIdScale, newAdmin)
        }
    }

    override suspend fun adminWithdraw(amount: Balance) {
        client.callTransaction(contractConfig.getMethodHashByName("admin_withdraw")) {
            write(BalanceScale, amount)
        }
    }
}