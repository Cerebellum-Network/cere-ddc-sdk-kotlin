package network.cere.ddc.contract.blockchain.model

import java.util.*

data class ChainMetadata(
    val magic: Int,
    val version: Int,
    val modules: List<Module>
) {

    data class Module(
        val name: String,
        val storage: Storage?,
        val calls: List<Call>,
        val events: List<Event>,
        val constants: List<Constant>,
        val errors: List<Error>,
        val index: Int
    )

    data class Storage(
        val prefix: String,
        val entries: List<Entry>
    )


    data class Entry(
        val name: String,
        val modifier: Modifier,
        val type: Type<*>,
        val defaults: ByteArray,
        val documentation: List<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Entry

            if (name != other.name) return false
            if (modifier != other.modifier) return false
            if (type != other.type) return false
            if (!defaults.contentEquals(other.defaults)) return false
            if (documentation != other.documentation) return false

            return true
        }

        override fun hashCode() =
            31 * Objects.hash(name, modifier, type, defaults, documentation) + defaults.contentHashCode()
    }

    enum class Modifier {
        OPTIONAL, DEFAULT, REQUIRED
    }

    enum class Hasher {
        BLAKE2_128, BLAKE2_256, BLAKE2_256_CONCAT, TWOX_128, TWOX_256, TWOX_64_CONCAT, IDENTITY
    }

    enum class TypeId(val clazz: Class<*>) {
        PLAIN(String::class.java), MAP(MapDefinition::class.java), DOUBLEMAP(DoubleMapDefinition::class.java);

    }

    abstract class Type<T>(private val value: T) {
        abstract val id: TypeId

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Type<*>

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value?.hashCode() ?: 0
        }

        override fun toString() = id.toString()

    }

    class PlainType(value: String) : Type<String>(value) {
        override val id = TypeId.PLAIN
    }

    class MapType(value: MapDefinition) : Type<MapDefinition>(value) {
        override val id = TypeId.MAP
    }

    class DoubleMapType(value: DoubleMapDefinition) : Type<DoubleMapDefinition>(value) {
        override val id = TypeId.DOUBLEMAP
    }

    data class MapDefinition(
        val hasher: Hasher,
        val key: String,
        val type: String,
        val isIterable: Boolean = false
    )


    data class DoubleMapDefinition(
        val firstHasher: Hasher,
        val firstKey: String,
        val secondHasher: Hasher,
        val secondKey: String,
        val type: String
    )

    data class Call(
        val index: Int = 0,
        val name: String,
        val arguments: List<Arg>,
        val documentation: List<String>
    )

    data class Arg(
        val name: String,
        val type: String
    )

    data class Event(
        val name: String,
        val arguments: List<String>,
        val documentation: List<String>
    )

    data class Constant(
        val name: String,
        val type: String,
        val value: ByteArray,
        val documentation: List<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Constant

            if (name != other.name) return false
            if (type != other.type) return false
            if (!value.contentEquals(other.value)) return false
            if (documentation != other.documentation) return false

            return true
        }

        override fun hashCode() = 31 * Objects.hash(name, type, documentation) + value.contentHashCode()
    }

    data class Error(
        val name: String,
        val documentation: List<String>
    )
}
