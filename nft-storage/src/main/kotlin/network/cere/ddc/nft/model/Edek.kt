package network.cere.ddc.nft.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Edek(
    @field: JsonProperty("publicKey")
    val publicKey: String? = null,
    @field: JsonProperty("value")
    val value: String? = null,
    @field: JsonProperty("metadataCid")
    val metadataCid: String? = null,
)
