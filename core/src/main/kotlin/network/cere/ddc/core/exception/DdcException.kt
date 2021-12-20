package network.cere.ddc.core.exception

open class DdcException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String) : this(message, null)
    constructor(cause: Throwable) : this(null, cause)
    constructor() : this(null, null)

}