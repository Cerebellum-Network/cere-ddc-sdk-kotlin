package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.reader.ListReader

class VectorSkipReader(private val skipReaderGenerator: SkipReaderGenerator) : SkipReader {

    private companion object {
        const val STARTS_WITH = "Vec<"
    }

    override fun predicate(typeName: String) = typeName.startsWith(STARTS_WITH)

    override fun buildScaleReader(typeName: String) =
        ListReader(
            skipReaderGenerator.generateScaleReader(typeName.substring(STARTS_WITH.length, typeName.length - 1))
        )
}