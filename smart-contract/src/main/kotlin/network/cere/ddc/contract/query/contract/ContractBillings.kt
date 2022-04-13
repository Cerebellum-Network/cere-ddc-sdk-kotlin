package network.cere.ddc.contract.query.contract

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.event.DepositEventReader
import network.cere.ddc.contract.mapping.response.AccountReader
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.BucketSmartContractError
import network.cere.ddc.contract.model.event.DepositEvent
import network.cere.ddc.contract.model.response.Account
import network.cere.ddc.contract.query.command.Billings

class ContractBillings(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    Billings {

    override suspend fun accountDeposit(value: Balance): DepositEvent {
        val event = client.callTransaction(contractConfig.accountDepositHash)

        return event.read(DepositEventReader)
    }

    override suspend fun accountGet(accountId: AccountId): Account {
        val response = client.call(contractConfig.accountGetHash) {
            write(AccountIdScale, accountId)
        }

        return response.checkError(BucketSmartContractError.values()).read(AccountReader)
    }
}