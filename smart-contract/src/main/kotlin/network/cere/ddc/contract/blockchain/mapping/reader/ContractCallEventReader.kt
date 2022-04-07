package network.cere.ddc.contract.blockchain.mapping.reader

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import network.cere.ddc.contract.blockchain.mapping.IndexedScaleReader
import network.cere.ddc.contract.blockchain.model.ContractCallEvent
import network.cere.ddc.contract.mapping.AccountIdReader

object ContractCallEventReader : IndexedScaleReader<ContractCallEvent> {

    override val moduleId: Int = 18
    override val actionId: Int = 5

    override fun read(reader: ScaleCodecReader) = ContractCallEvent(
        executor = reader.read(AccountIdReader),
        data = reader.readByteArray()
    )
}