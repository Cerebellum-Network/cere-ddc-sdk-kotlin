package network.cere.ddc.crypto.security

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import network.cere.ddc.crypto.extension.sha256
import network.cere.ddc.crypto.model.Node
import network.cere.ddc.crypto.signature.Scheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HttpSecurity {
    var scheme: Scheme? = null
    var expiresAfter: Duration = Duration.ZERO
    var nodes: List<Node> = listOf()
    var signedHeaders: List<String> = listOf()


    companion object Feature : HttpClientFeature<HttpSecurity, HttpSecurity> {

        private const val CONTENT_SHA_256_HEADER = "Content-SHA256"

        override val key: AttributeKey<HttpSecurity> = AttributeKey("HttpSecurityFeature")

        override fun install(feature: HttpSecurity, scope: HttpClient) {
            val mapNodes = feature.nodes.associate { it.address to it.id }

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                val expireDate =
                    LocalDateTime.now().plus(feature.expiresAfter).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                val nodeId = mapNodes[context.url.host]

                context.url.protocol = URLProtocol.HTTPS

                context.headers {
                    set("Expires", expireDate)
                    set("Signing-Algorithm", feature.scheme!!.name)
                    set("Client-Public-Key", feature.scheme!!.publicKey)
                    set("Node-Id", nodeId ?: throw IllegalArgumentException("Node ${context.url.host} doesn't exist"))
                    feature.signedHeaders.forEach { append("Signed-Headers", it) }

                    val signedHeaders = feature.signedHeaders.asSequence()
                        .map { "$it=${this[it]?.trim() ?: throw IllegalArgumentException("Invalid signed header $it")}" }
                        .joinToString(separator = ";")
                    val content =
                        "${context.method}\n${context.url.encodedPath}\n${nodeId}\n${expireDate}\n${signedHeaders}\n${this[CONTENT_SHA_256_HEADER] ?: (context.body as ByteArray).sha256()}"

                    set("Request-Signature", feature.scheme!!.sign(content.toByteArray()))
                }
            }
        }

        override fun prepare(block: HttpSecurity.() -> Unit) = HttpSecurity().apply(block).also {
            if (it.scheme == null || it.nodes.isEmpty() || it.expiresAfter.isZero) {
                throw IllegalStateException("Invalid configuration: $key")
            }
        }
    }
}




