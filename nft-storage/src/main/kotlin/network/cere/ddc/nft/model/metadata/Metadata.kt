package network.cere.ddc.nft.model.metadata

interface Metadata {
    val schema: String

    companion object {
        fun getAllMetadata() = mapOf(
            Erc721Metadata.SCHEMA_NAME.lowercase() to Erc721Metadata::class,
            Erc1155Metadata.SCHEMA_NAME.lowercase() to Erc1155Metadata::class
        )
    }
}