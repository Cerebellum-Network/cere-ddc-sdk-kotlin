package network.cere.ddc.contract.blockchain.mapping.reader.skip

import io.emeraldpay.polkaj.scale.ScaleCodecReader

class CompactSkipReader : SkipReader {

    private companion object {
        const val STARTS_WITH = "Compact<"
    }

    override fun predicate(typeName: String) = typeName.startsWith(STARTS_WITH)

    override fun buildScaleReader(typeName: String) = ScaleCodecReader.COMPACT_BIGINT
}