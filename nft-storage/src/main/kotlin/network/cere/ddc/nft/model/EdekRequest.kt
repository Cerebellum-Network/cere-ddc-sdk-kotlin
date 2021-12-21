package network.cere.ddc.nft.model

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class EdekRequest(
    val metadataCid: String,
    @JsonUnwrapped
    val edek: Edek
)
