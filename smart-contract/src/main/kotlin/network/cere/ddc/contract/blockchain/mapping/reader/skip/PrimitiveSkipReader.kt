package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleReader

class PrimitiveSkipReader : SkipReader {

    private val primitiveTypes = mapOf(
        "u8" to 1,
        "u16" to 2,
        "u32" to 4,
        "u64" to 8,
        "u128" to 16,
        "u160" to 20,
        "u256" to 32,
        "Null" to 0,
    )

    override fun predicate(typeName: String): Boolean {
        return primitiveTypes.containsKey(typeName)
    }

    override fun buildScaleReader(typeName: String): ScaleReader<*> {
        val skipBytes = primitiveTypes.getValue(typeName)
        return ScaleReader { it.skip(skipBytes) }
    }
}