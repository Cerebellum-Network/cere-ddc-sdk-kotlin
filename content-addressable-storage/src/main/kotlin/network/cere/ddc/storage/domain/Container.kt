package network.cere.ddc.storage.domain

interface Container {
    var data: ByteArray
    val tags: List<Tag>
    val cid: String?
}