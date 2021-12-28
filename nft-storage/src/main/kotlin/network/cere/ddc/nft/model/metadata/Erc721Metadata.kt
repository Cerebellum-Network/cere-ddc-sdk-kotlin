package network.cere.ddc.nft.model.metadata

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Erc721Metadata(
    @field: JsonProperty("name")
    val name: String? = null,
    @field: JsonProperty("description")
    val description: String? = null,
    @field: JsonProperty("image")
    val image: String? = null,
) : Metadata {

    companion object {
        const val SCHEMA_NAME = "ERC-721"
    }

    @JsonIgnore
    override val schema = SCHEMA_NAME
}