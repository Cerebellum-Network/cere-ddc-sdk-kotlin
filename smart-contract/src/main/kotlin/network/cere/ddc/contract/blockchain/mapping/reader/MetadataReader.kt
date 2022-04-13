package network.cere.ddc.contract.blockchain.mapping.reader

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.EnumReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import io.emeraldpay.polkaj.scale.reader.UnionReader
import network.cere.ddc.contract.blockchain.mapping.readNullable
import network.cere.ddc.contract.blockchain.model.ChainMetadata
import network.cere.ddc.contract.blockchain.model.ChainMetadata.*


object MetadataReader : ScaleReader<ChainMetadata> {
    val MODULE_LIST_READER = ListReader(ModulesReader)
    val STRING_LIST_READER = ListReader(ScaleCodecReader.STRING)
    val HASHER_ENUM_READER = EnumReader(Hasher.values())

    override fun read(rdr: ScaleCodecReader) = ChainMetadata(
        magic = ScaleCodecReader.INT32.read(rdr),
        version = rdr.readUByte().also { check(it == 12) { "Unsupported metadata version: $it" } },
        modules = calculateCalls(MODULE_LIST_READER.read(rdr))
    )

    private fun calculateCalls(modules: List<Module>) =
        modules.map { module ->
            val calls = module.calls
                .mapIndexed { i, call -> call.copy(index = (module.index shl 8) + i) }
                .sortedBy { it.index }
            module.copy(calls = calls)
        }.sortedBy { it.index }

    private object ModulesReader : ScaleReader<Module> {

        val CALL_LIST_READER = ListReader(CallReader)
        val EVENT_LIST_READER = ListReader(EventReader)
        val CONSTANT_LIST_READER = ListReader(ConstantReader)
        val ERROR_LIST_READER = ListReader(ErrorReader)

        override fun read(rdr: ScaleCodecReader) = Module(
            name = rdr.readString(),
            storage = rdr.readNullable(StorageReader),
            calls = rdr.readNullable(CALL_LIST_READER) ?: listOf(),
            events = rdr.readNullable(EVENT_LIST_READER) ?: listOf(),
            constants = CONSTANT_LIST_READER.read(rdr),
            errors = ERROR_LIST_READER.read(rdr),
            index = rdr.readUByte()
        )
    }

    private object StorageReader : ScaleReader<Storage> {

        val ENTRY_LIST_READER = ListReader(StorageEntryReader)

        override fun read(rdr: ScaleCodecReader) = Storage(
            prefix = rdr.readString(),
            entries = ENTRY_LIST_READER.read(rdr)
        )
    }

    private object StorageEntryReader : ScaleReader<Entry> {

        val MODIFIER_ENUM_READER = EnumReader(Modifier.values())

        override fun read(rdr: ScaleCodecReader) = Entry(
            name = rdr.readString(),
            modifier = MODIFIER_ENUM_READER.read(rdr),
            type = rdr.read(TypeReader),
            defaults = rdr.readByteArray(),
            documentation = STRING_LIST_READER.read(rdr)
        )
    }

    private object TypeReader : ScaleReader<Type<*>> {

        val TYPE_UNION_READER = UnionReader(TypePlainReader, TypeMapReader, TypeDoubleMapReader)

        override fun read(rdr: ScaleCodecReader): Type<*> {
            return TYPE_UNION_READER.read(rdr).value
        }
    }

    private object TypePlainReader : ScaleReader<PlainType> {
        override fun read(rdr: ScaleCodecReader): PlainType {
            return PlainType(rdr.readString())
        }
    }

    private object TypeMapReader : ScaleReader<MapType> {
        override fun read(rdr: ScaleCodecReader) = MapType(
            MapDefinition(
                hasher = HASHER_ENUM_READER.read(rdr),
                key = rdr.readString(),
                type = rdr.readString(),
                isIterable = rdr.readBoolean()
            )
        )
    }

    private object TypeDoubleMapReader : ScaleReader<DoubleMapType> {
        override fun read(rdr: ScaleCodecReader) = DoubleMapType(
            DoubleMapDefinition(
                firstHasher = HASHER_ENUM_READER.read(rdr),
                firstKey = rdr.readString(),
                secondKey = rdr.readString(),
                type = rdr.readString(),
                secondHasher = HASHER_ENUM_READER.read(rdr)
            )
        )
    }

    private object CallReader : ScaleReader<Call> {

        val ARG_LIST_READER = ListReader(ArgReader)

        override fun read(rdr: ScaleCodecReader) = Call(
            name = rdr.readString(),
            arguments = ARG_LIST_READER.read(rdr),
            documentation = STRING_LIST_READER.read(rdr)
        )
    }

    private object ArgReader : ScaleReader<Arg> {
        override fun read(rdr: ScaleCodecReader) = Arg(
            name = rdr.readString(),
            type = rdr.readString()
        )
    }

    private object EventReader : ScaleReader<Event> {
        override fun read(rdr: ScaleCodecReader) = Event(
            name = rdr.readString(),
            arguments = STRING_LIST_READER.read(rdr),
            documentation = STRING_LIST_READER.read(rdr)
        )
    }

    private object ConstantReader : ScaleReader<Constant> {
        override fun read(rdr: ScaleCodecReader) = Constant(
            name = rdr.readString(),
            type = rdr.readString(),
            value = rdr.readByteArray(),
            documentation = STRING_LIST_READER.read(rdr)
        )
    }

    private object ErrorReader : ScaleReader<Error> {
        override fun read(rdr: ScaleCodecReader) = Error(
            name = rdr.readString(),
            documentation = STRING_LIST_READER.read(rdr)
        )
    }

}
