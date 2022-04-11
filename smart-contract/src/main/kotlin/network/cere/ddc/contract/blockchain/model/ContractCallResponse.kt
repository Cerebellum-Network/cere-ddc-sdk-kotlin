package network.cere.ddc.contract.blockchain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ContractCallResponse(
    @JsonProperty("success")
    val success: Success?,
    @JsonProperty("error")
    val error: Error?
) {
    data class Success(
        @JsonProperty("status")
        val status: Int,
        @JsonProperty("data")
        val data: String,
        @JsonProperty("gas_consumed")
        val gasConsumed: Long
    )

    data class Error(
        @JsonProperty("code")
        val code: Int,
        @JsonProperty("message")
        val message: String
    )
}
