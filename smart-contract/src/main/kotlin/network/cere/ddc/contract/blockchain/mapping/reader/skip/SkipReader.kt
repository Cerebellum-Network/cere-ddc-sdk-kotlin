package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleReader

interface SkipReader {

    fun predicate(typeName: String): Boolean

    fun buildScaleReader(typeName: String): ScaleReader<*>
}