package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleReader

class OptionSkipReader(private val skipReaderGenerator: SkipReaderGenerator) : SkipReader {

    private companion object {
        const val STARTS_WITH = "Option<"
    }

    override fun predicate(typeName: String) = typeName.startsWith(STARTS_WITH)

    override fun buildScaleReader(typeName: String): ScaleReader<*> {
        val reader =
            skipReaderGenerator.generateScaleReader(typeName.substring(STARTS_WITH.length, typeName.length - 1))
        return ScaleReader { it.readOptional(reader) }
    }
}