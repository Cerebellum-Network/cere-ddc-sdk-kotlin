package network.cere.ddc.nft.model.metadata

import com.fasterxml.jackson.annotation.JsonIgnore

data class Erc721Metadata(
    val name: String,
    val description: String,
    val image: String,
) : Metadata {
    @JsonIgnore
    override val schema = "ERC-721"
}