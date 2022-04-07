package network.cere.ddc.contract.blockchain.mapping.reader

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.EnumReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import io.emeraldpay.polkaj.types.Hash256
import network.cere.ddc.contract.blockchain.mapping.IndexedScaleReader
import network.cere.ddc.contract.blockchain.mapping.SkipReaderGenerator
import network.cere.ddc.contract.blockchain.model.ChainMetadata
import network.cere.ddc.contract.blockchain.model.EventRecord

class EventReader<T>(
    private val indexedReader: IndexedScaleReader<T>,
    private val metadata: ChainMetadata,
    private val skipReaderGenerator: SkipReaderGenerator
) : ScaleReader<EventRecord<T>> {

    private val phaseReader = EnumReader(EventRecord.Phase.values())
    private val topicsReader = ListReader(Hash256Reader)

    override fun read(reader: ScaleCodecReader): EventRecord<T> {
        val phase = reader.read(phaseReader)
        val id = reader.readUint32()
        val moduleId = reader.readUByte()
        val eventId = reader.readUByte()
        val event = reader.readEvent(moduleId, eventId)
        val topics = reader.read(topicsReader)

        return EventRecord(
            phase = phase,
            id = id,
            moduleId = moduleId,
            eventId = eventId,
            event = event,
            topics = topics
        )
    }

    private fun ScaleCodecReader.readEvent(moduleId: Int, eventId: Int): T? {
        if (moduleId == indexedReader.moduleId && eventId == indexedReader.actionId) {
            return read(indexedReader)
        } else {
            metadata.modules[moduleId].events[eventId].arguments.forEach {
                read(skipReaderGenerator.generateReader(it))
            }
            return null
        }
    }

    object Hash256Reader : ScaleReader<Hash256> {
        override fun read(reader: ScaleCodecReader) = Hash256(reader.readUint256())
    }
}