package network.cere.ddc.core.http

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.util.*
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.signature.Scheme
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HttpSecurity {
    var scheme: Scheme? = null
    var expiresAfter: Duration = Duration.ZERO
    var nodeAddresses = mapOf<String, String>()
    var signedHeaders: List<String> = listOf()

    companion object Feature : HttpClientFeature<HttpSecurity, HttpSecurity> {

        private const val CONTENT_SHA_256_HEADER = "Content-SHA256"

        override val key: AttributeKey<HttpSecurity> = AttributeKey("HttpSecurityFeature")

        override fun install(feature: HttpSecurity, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State)  {
                val expireDate =
                    ZonedDateTime.now().plus(feature.expiresAfter).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                val nodeId =
                    feature.nodeAddresses["${context.url.protocol.name}://${context.url.host}:${context.url.port}"]
                        ?: throw IllegalArgumentException("Node ${context.url.host} doesn't exist")

                context.headers {
                    set("Expires", expireDate)
                    set("Signing-Algorithm", feature.scheme!!.name)
                    set("Client-Public-Key", feature.scheme!!.publicKey)
                    set("Node-Id", nodeId)
                    feature.signedHeaders.forEach { append("Signed-Headers", it) }

                    val signedHeaders = feature.signedHeaders.asSequence()
                        .map { "$it=${this[it]?.trim() ?: throw IllegalArgumentException("Invalid signed header $it")}" }
                        .joinToString(separator = ";")

                    val bodyHash = this[CONTENT_SHA_256_HEADER]
                        ?: context.body.let { if (it is ByteArray) it else if (it is EmptyContent) "".toByteArray() else it.toString().toByteArray() }.sha256()
                    val content =
                        "${context.method.value}\n${context.url.encodedPath}\n${nodeId}\n${expireDate}\n${signedHeaders}\n$bodyHash"

                    set("Request-Signature", feature.scheme!!.sign(content.toByteArray()))
                }
            }
        }

        override fun prepare(block: HttpSecurity.() -> Unit) = HttpSecurity().apply(block).also {
            if (it.scheme == null || it.nodeAddresses.isEmpty() || it.expiresAfter.isZero) {
                throw IllegalStateException("Invalid configuration: $key")
            }
        }
    }
}




