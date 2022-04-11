package network.cere.ddc.contract.blockchain.mapping.reader.skip

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.emeraldpay.polkaj.scale.ScaleReader

class ObjectSkipReader(private val types: JsonNode, private val skipReaderGenerator: SkipReaderGenerator) : SkipReader {

    override fun predicate(typeName: String) = types.has(typeName)

    override fun buildScaleReader(typeName: String): ScaleReader<*> {
        val objectType = types.get(typeName)

        val structure = objectType.get("type")?.asText()
        return when (structure) {
            null -> skipReaderGenerator.generateScaleReader(objectType.asText())
            "struct" -> objectType.withArray<ArrayNode>("type_mapping").map {
                skipReaderGenerator.generateScaleReader(it.get(1).asText())
            }.let { readers -> ScaleReader { mainReader -> readers.forEach { mainReader.read(it) } } }
            "enum" -> ScaleReader { it.skip(1) }
            else -> throw IllegalArgumentException("Unknown object structure $structure")
        }
    }
}