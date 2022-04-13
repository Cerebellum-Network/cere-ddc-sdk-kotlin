package network.cere.ddc.contract.mapping.response

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.model.response.NodeStatus

object NodeStatusReader : ScaleReader<NodeStatus> {

    override fun read(reader: ScaleCodecReader) = NodeStatus(
        nodeId = reader.readUint32(),
        node = reader.read(NodeReader),
        params = reader.readString()
    )

    private object NodeReader : ScaleReader<NodeStatus.Node> {
        override fun read(reader: ScaleCodecReader) = NodeStatus.Node(
            providerId = reader.read(AccountIdScale),
            rentPerMonth = reader.read(BalanceScale),
            freeResource = reader.readUint32()
        )
    }
}