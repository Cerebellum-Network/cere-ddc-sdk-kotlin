package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.model.ClusterStatus

object ClusterStatusReader : ScaleReader<ClusterStatus> {

    override fun read(reader: ScaleCodecReader) = ClusterStatus(
        clusterId = reader.readUint32(),
        cluster = reader.read(ClusterReader),
        params = reader.readString()
    )

    private object ClusterReader : ScaleReader<ClusterStatus.Cluster> {

        private val vnodesListReader = ListReader(ScaleCodecReader.UINT32)

        override fun read(reader: ScaleCodecReader) = ClusterStatus.Cluster(
            managerId = reader.read(AccountIdScale),
            vnodes = reader.read(vnodesListReader),
            resourcePerVnode = reader.readUint32(),
            resourceUsed = reader.readUint32(),
            revenues = reader.read(BalanceScale),
            totalRent = reader.read(BalanceScale)
        )
    }
}