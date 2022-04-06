package network.cere.ddc.contract.blockchain.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.EnumReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import io.emeraldpay.polkaj.types.Hash256
import network.cere.ddc.contract.blockchain.model.EventRecord
import org.komputing.khex.extensions.toNoPrefixHexString

object ExtrinsicEventReader : ScaleReader<EventRecord> {

    private val phaseReader = EnumReader(EventRecord.Phase.values())
    private val topicsReader = ListReader(Hash256Reader)

    override fun read(reader: ScaleCodecReader) = EventRecord(
        phase = reader.read(phaseReader),
        id = reader.readUint32(),
        type = reader.readByteArray(2).toNoPrefixHexString(),
        attributes = reader.readByteArray(),
        topics = reader.read(topicsReader)
    )


    object Hash256Reader : ScaleReader<Hash256> {
        override fun read(reader: ScaleCodecReader) = Hash256(reader.readUint256())
    }
}