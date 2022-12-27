package network.cere.ddc.storage.domain

data class Tag(
    val key: String,
    val value: String,
    val searchType: SearchType = SearchType.RANGE
    )
enum class SearchType {
    RANGE,
    NOT_SEARCHABLE
}