package network.cere.ddc.contract.model.response

data class ResultList<T>(
    val totalLength: Long,
    val result: List<T>
) : List<T> by result
