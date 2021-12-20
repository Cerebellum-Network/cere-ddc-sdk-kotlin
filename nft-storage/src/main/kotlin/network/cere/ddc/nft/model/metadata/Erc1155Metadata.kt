package network.cere.ddc.nft.model.metadata

import com.fasterxml.jackson.annotation.JsonIgnore

data class Erc1155Metadata(
    val name: String,
    val description: String,
    val image: String,
    val decimals: Int,
    val properties: Map<String, Any> = mapOf()
) : Metadata {
    @JsonIgnore
    override val schema = "ERC-1155"
}

