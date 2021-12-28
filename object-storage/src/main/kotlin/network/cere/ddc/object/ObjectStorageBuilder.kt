package network.cere.ddc.`object`

import io.ktor.client.*
import network.cere.ddc.`object`.client.HttpTransportClient
import network.cere.ddc.`object`.config.ObjectStorageConfig
import network.cere.ddc.`object`.config.TransportClientConfig
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.signature.Scheme.Companion.ED_25519

class ObjectStorageBuilder {

    var trustedNodes = listOf<Node>()
        private set
    var transportClientConfig = TransportClientConfig()
        private set
    var objectStorageConfig = ObjectStorageConfig()
        private set
    var scheme: String = ED_25519
        private set
    var privateKey: String = ""
        private set
    var httpClient: HttpClient = HttpClient()
        private set

    fun trustedNodes(trustedNodes: List<Node>) = apply { this.trustedNodes = trustedNodes }

    fun transportClientConfig(transportClientConfig: TransportClientConfig) =
        apply { this.transportClientConfig = transportClientConfig }

    fun objectStorageConfig(objectStorageConfig: ObjectStorageConfig) =
        apply { this.objectStorageConfig = objectStorageConfig }

    fun scheme(scheme: String) = apply { this.scheme = scheme }

    fun privateKey(privateKey: String) = apply { this.privateKey = privateKey }

    fun httpClient(httpClient: HttpClient) = apply { this.httpClient = httpClient }

    fun build(): ObjectStorage =
        HttpTransportClient(Scheme.create(scheme, privateKey), trustedNodes, transportClientConfig, httpClient)
            .let { ObjectStorage(it, objectStorageConfig) }
}