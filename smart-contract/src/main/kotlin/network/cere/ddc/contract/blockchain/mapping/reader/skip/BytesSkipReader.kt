package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleReader

class BytesSkipReader : SkipReader {

    override fun predicate(typeName: String) = typeName == "Bytes"

    override fun buildScaleReader(typeName: String) = ScaleReader { it.skip(it.readCompactInt()) }
}