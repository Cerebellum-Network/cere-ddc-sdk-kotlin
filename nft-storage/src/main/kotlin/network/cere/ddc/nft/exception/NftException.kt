package network.cere.ddc.nft.exception

import network.cere.ddc.core.exception.DdcException

open class NftException(message: String?, cause: Throwable?) : DdcException(message, cause) {

    constructor(message: String) : this(message, null)
    constructor(cause: Throwable) : this(null, cause)
    constructor() : this(null, null)

}