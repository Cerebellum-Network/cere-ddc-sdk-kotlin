package network.cere.ddc.contract.blockchain.mapping.reader.skip

import com.fasterxml.jackson.databind.JsonNode
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.emeraldpay.polkaj.scale.ScaleReader

class SkipReaderGenerator(
    types: JsonNode,
    private val cache: Cache<String, ScaleReader<*>> = Caffeine.newBuilder().maximumSize(50).build()
) {

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

    fun generateScaleReader(typeName: String): ScaleReader<*> {
        return cache.getIfPresent(typeName)
            ?: skipReaders.find { it.predicate(typeName) }?.buildScaleReader(typeName)?.also { cache.put(typeName, it) }
            ?: throw IllegalArgumentException("Unable to parse type $typeName")
    }
}