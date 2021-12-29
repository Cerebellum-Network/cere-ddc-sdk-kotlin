package network.cere.ddc.contract.exception

import network.cere.ddc.core.exception.DdcException

open class SmartContractException(message: String? = null, cause: Throwable? = null) : DdcException(message, cause)