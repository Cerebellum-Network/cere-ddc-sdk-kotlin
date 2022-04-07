package network.cere.ddc.contract.blockchain.mapping

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader

class SkipReaderGenerator(private val types: JsonNode) {

    private val primitiveTypes =
        mapOf(
            "u8" to 1,
            "u16" to 2,
            "u32" to 4,
            "u64" to 8,
            "u128" to 16,
            "u160" to 20,
            "u256" to 32,
            "Null" to 0,
        )

    //ToDo add caching
    fun generateReader(type: String): ScaleReader<*> {
        return generateForPrimitive(type)
            ?: generateForBool(type)
            ?: generateForBytes(type)
            ?: generateForCompact(type)
            ?: generateForArray(type)
            ?: generateForOption(type)
            ?: generateForVector(type)
            ?: generateForObject(type)
            ?: throw IllegalArgumentException("Unable parse type $type")
    }

    private fun generateForObject(type: String): ScaleReader<*>? {
        val specificType = types.get(type) ?: return null

        return when (specificType.get("type")?.asText()) {
            null -> generateReader(specificType.asText())
            "struct" -> specificType.withArray<ArrayNode>("type_mapping").map {
                generateReader(it.get(1).asText())
            }.let { readers -> ScaleReader { mainReader -> readers.forEach { mainReader.read(it) } } }
            "enum" -> ScaleReader { it.skip(1) }
            else -> null
        }
    }

    private fun generateForPrimitive(type: String): ScaleReader<*>? {
        val primitiveTypeSkip = primitiveTypes[type]

        return if (primitiveTypeSkip != null) {
            ScaleReader { it.skip(primitiveTypeSkip) }
        } else {
            null
        }
    }

    private fun generateForBool(type: String): ScaleReader<*>? {
        return if (type == "bool") {
            ScaleCodecReader.BOOL
        } else {
            null
        }
    }

    private fun generateForBytes(type: String): ScaleReader<*>? {
        return if (type == "Bytes") {
            ScaleReader { it.skip(it.readCompactInt()) }
        } else {
            null
        }
    }

    private fun generateForCompact(type: String): ScaleReader<*>? {
        val start = "Compact<"
        val end = ">"

        return if (type.startsWith(start) and type.endsWith(end)) {
            ScaleCodecReader.COMPACT_BIGINT
        } else {
            null
        }
    }

    private fun generateForArray(type: String): ScaleReader<*>? {
        val start = "["
        val end = "]"

        return if (type.startsWith(start) and type.endsWith(end)) {
            val values = type.substring(start.length, type.length - end.length).split(";")
            val reader = generateReader(values[0].trim())
            val number = values[1].trim().toInt()

            ScaleReader { scaleReader -> repeat(number) { scaleReader.read(reader) } }
        } else {
            null
        }
    }

    private fun generateForOption(type: String): ScaleReader<*>? {
        val start = "Option<"
        val end = ">"

        return if (type.startsWith(start) and type.endsWith(end)) {
            val reader = generateReader(type.substring(start.length, type.length - end.length))
            ScaleReader { it.readOptional(reader) }
        } else {
            null
        }
    }

    private fun generateForVector(type: String): ScaleReader<*>? {
        val start = "Vec<"
        val end = ">"

        return if (type.startsWith(start) and type.endsWith(end)) {
            ListReader(generateReader(type.substring(start.length, type.length - end.length)))
        } else {
            null
        }
    }

}