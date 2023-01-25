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

    suspend fun adminGrantPermission(accountId: AccountId, permission: Boolean) {
        client.callTransaction(contractConfig.getMethodHashByName("admin_change"), permission) {
            write(AccountIdScale, accountId)
        }
    }

    override suspend fun adminWithdraw(amount: Balance, predictGasLimit: Boolean) {
        client.callTransaction(contractConfig.getMethodHashByName("admin_withdraw"), predictGasLimit) {
            write(BalanceScale, amount)
        }
    }
}