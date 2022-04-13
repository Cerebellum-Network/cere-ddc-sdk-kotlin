package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader

class BooleanSkipReader : SkipReader {

    override fun predicate(typeName: String) = typeName == "bool"

    override fun buildScaleReader(typeName: String): ScaleReader<Boolean> = ScaleCodecReader.BOOL

}