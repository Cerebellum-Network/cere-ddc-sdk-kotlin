package network.cere.ddc.contract.model

data class ResultList<T>(
    val totalLength: Long,
    val result: List<T>
) : List<T> by result
