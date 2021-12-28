package network.cere.ddc.`object`.exception

import network.cere.ddc.core.exception.DdcException

open class ObjectException(message: String? = null, cause: Throwable? = null) : DdcException(message, cause)