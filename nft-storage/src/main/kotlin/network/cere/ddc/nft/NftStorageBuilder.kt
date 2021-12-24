package network.cere.ddc.nft

import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.signature.Scheme.Companion.ED_25519
import network.cere.ddc.nft.client.HttpTransportClient
import network.cere.ddc.nft.config.NftStorageConfig
import network.cere.ddc.nft.config.TransportClientConfig

class NftStorageBuilder {

    var trustedNodes = listOf<Node>()
        private set
    var transportClientConfig = TransportClientConfig()
        private set
    var nftStorageConfig = NftStorageConfig()
        private set
    var scheme: String = ED_25519
        private set
    var privateKey: String = ""
        private set

    fun trustedNodes(trustedNodes: List<Node>) = apply { this.trustedNodes = trustedNodes }

    fun transportClientConfig(transportClientConfig: TransportClientConfig) =
        apply { this.transportClientConfig = transportClientConfig }

    fun nftStorageConfig(nftStorageConfig: NftStorageConfig) = apply { this.nftStorageConfig = nftStorageConfig }

    fun scheme(scheme: String) = apply { this.scheme = scheme }

    fun privateKey(privateKey: String) = apply { this.privateKey = privateKey }

    fun build(): NftStorage =
        HttpTransportClient(Scheme.create(scheme, privateKey), trustedNodes, transportClientConfig)
            .let { NftStorage(it, nftStorageConfig) }
}