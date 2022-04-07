package network.cere.ddc.contract.blockchain.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.ScaleWriter
import io.emeraldpay.polkaj.scale.reader.BoolOptionalReader
import io.emeraldpay.polkaj.scale.reader.BoolReader
import network.cere.ddc.contract.blockchain.exception.SmartContractException
import java.util.*

inline fun <reified T> ScaleCodecReader.readNullable(scaleReader: ScaleReader<T>): T? {
    return if (scaleReader !is BoolReader && scaleReader !is BoolOptionalReader) {
        if (readBoolean()) read(scaleReader) else null
    } else {
        ScaleCodecReader.BOOL_OPTIONAL.read(this).orElse(null) as T?
    }
}

fun ScaleCodecReader.checkError() = also {
    val resultCode = readUByte()

    if (resultCode != 0) {
        throw SmartContractException(resultCode, "Contract data has exception")
    }
}

fun ScaleCodecWriter.writeString(data: String) = writeAsList(data.toByteArray())

inline fun <reified T> ScaleCodecWriter.writeNullable(writer: ScaleWriter<T>, value: T?) =
    writeOptional(writer, Optional.ofNullable(value))