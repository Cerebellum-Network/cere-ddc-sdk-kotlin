package network.cere.ddc.contract.blockchain.mapping.reader.skip

import com.fasterxml.jackson.databind.JsonNode
import io.emeraldpay.polkaj.scale.ScaleReader

class SkipReaderGenerator(types: JsonNode) {

    private val skipReaders = listOf(
        PrimitiveSkipReader(),
        BooleanSkipReader(),
        BytesSkipReader(),
        CompactSkipReader(),
        ArraySkipReader(this),
        OptionSkipReader(this),
        VectorSkipReader(this),
        ObjectSkipReader(types, this)
    )

    //ToDo add caching
    fun generateScaleReader(typeName: String): ScaleReader<*> {
        return skipReaders.find { it.predicate(typeName) }?.buildScaleReader(typeName)
            ?: throw IllegalArgumentException("Unable to parse type $typeName")
    }
}