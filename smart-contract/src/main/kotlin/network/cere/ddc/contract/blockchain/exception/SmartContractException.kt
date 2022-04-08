package network.cere.ddc.contract.blockchain.exception

class SmartContractException(val typeError: Enum<*>, msg: String) : RuntimeException(msg)