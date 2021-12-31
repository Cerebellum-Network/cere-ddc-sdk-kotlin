package network.cere.ddc.`object`.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Edek(
    @field: JsonProperty("publicKey")
    val publicKey: String? = null,
    @field: JsonProperty("value")
    val value: String? = null,
    @field: JsonProperty("objectCid")
    val objectCid: String? = null,
)
