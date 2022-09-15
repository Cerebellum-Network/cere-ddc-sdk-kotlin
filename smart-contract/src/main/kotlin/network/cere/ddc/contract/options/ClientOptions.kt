package network.cere.ddc.contract.options

import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.encryption.Cipher
import network.cere.ddc.core.encryption.NaclCipher
import network.cere.ddc.core.signature.Scheme.Companion.SR_25519
import network.cere.ddc.storage.config.FileStorageConfig

data class ClientOptions(
    var clusterId: Long?,
    var cdnUrl: String,
    var fileOptions: FileStorageConfig = FileStorageConfig(),
    val smartContract: SmartContractOptions = SmartContractOptions.TESTNET,
    val schemeType: String = SR_25519,
    val cipher: Cipher = NaclCipher(),
    val cidBuilder: CidBuilder = CidBuilder()
) {
    constructor(clusterId: Long) : this(clusterId, "")
    constructor(cdnUrl: String) : this(null, cdnUrl)

}



