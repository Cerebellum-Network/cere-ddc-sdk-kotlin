package network.cere.ddc.nft.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class EdekRequest(
    @field: JsonProperty("metadataCid")
    val metadataCid: String? = null,
    @JsonUnwrapped
    val edek: Edek
)
