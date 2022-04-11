package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleReader

class ArraySkipReader(private val skipReaderGenerator: SkipReaderGenerator) : SkipReader {

    private companion object {
        const val STARTS_WITH = "["
        const val ENDS_WITH = "]"
    }

    override fun predicate(typeName: String) = typeName.startsWith(STARTS_WITH) && typeName.endsWith(ENDS_WITH)

    override fun buildScaleReader(typeName: String): ScaleReader<*> {
        val values = typeName.substring(STARTS_WITH.length, typeName.length - ENDS_WITH.length).split(";")
        val reader = skipReaderGenerator.generateScaleReader(values[0].trim())
        val number = values[1].trim().toInt()

        return ScaleReader { scaleReader -> repeat(number) { scaleReader.read(reader) } }
    }

}