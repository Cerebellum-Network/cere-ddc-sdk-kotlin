package network.cere.ddc.nft.model.metadata

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class Erc1155Metadata(
    @field: JsonProperty("name")
    val name: String? = null,
    @field: JsonProperty("description")
    val description: String? = null,
    @field: JsonProperty("image")
    val image: String? = null,
    @field: JsonProperty("decimals")
    val decimals: Int? = null,
    @field: [JsonProperty("properties") JsonInclude(JsonInclude.Include.NON_NULL)]
    val properties: Map<String, Any>? = null
) : Metadata {

    companion object {
        const val SCHEMA_NAME = "ERC-1155"
    }

    @JsonIgnore
    override val schema = SCHEMA_NAME
}

