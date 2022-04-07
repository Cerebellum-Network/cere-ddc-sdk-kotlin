package network.cere.ddc.contract.blockchain.exception

class SmartContractException(val index: Int, msg: String) : RuntimeException(msg)