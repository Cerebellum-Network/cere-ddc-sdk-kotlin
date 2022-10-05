package network.cere.ddc.core.uri

data class DdcUri(
    val bucketId: Long?,
    val cid: String?,
    val path: String?,
    val protocol: Protocol?,
    val organization: String?,
    val options: String?
) {
    companion object {
        const val DDC = "ddc"
        const val ORG = "org"
        const val BUC = "buc"
    }


    override fun toString(): String {
        val parts = mutableListOf<String>()
        parts.add("/${DDC}")

        if (organization != null) {
            parts.add(ORG)
            parts.add(organization)
        }

        if (bucketId != null) {
            parts.add(BUC)
            parts.add(""+bucketId)
        }

        if (path != null) {
            if (protocol == null ) {
                throw Exception("Unable to build DDC uri string without protocol");
            }
            parts.add(protocol.name)
            parts.add(path)
        }

        var result = ""
        parts.forEach{result = "$result/$it" }
        if (options != null) {
            result = "$result?$options"
        }

        return result;
    }

    data class Builder(

        var bucketId: Long?  = null,
        var cid: String?  = null,
        var path: String?  = null,
        var protocol: Protocol?  = null,
        var organization: String?  = null,
        var options: String?  = null,
        ) {

        fun bucketId(bucketId: Long) = apply { this.bucketId = bucketId }
        fun cid(cid: String) = apply { this.cid = cid }
        fun path(path: String) = apply { this.path = path }
        fun protocol(protocol: Protocol) = apply { this.protocol = protocol }
        fun organization(organization: String) = apply { this.organization = organization }
        fun options(options: String) = apply { this.options = options }
        fun build() = DdcUri(bucketId, cid, path, protocol, organization, options)
    }

}
