package network.cere.ddc.contract.mapping.event

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader

abstract class AbstractEventReader<T> : ScaleReader<T> {
    protected abstract val index: Int

    abstract fun parse(reader: ScaleCodecReader): T

    override fun read(reader: ScaleCodecReader): T {
        val id = reader.readUByte()
        if (id != index) {
            throw RuntimeException("Event with id=$id can't be read")
        }

        return parse(reader)
    }

}