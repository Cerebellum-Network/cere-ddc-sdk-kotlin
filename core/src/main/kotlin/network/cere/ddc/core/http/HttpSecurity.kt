package network.cere.ddc.core.http

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
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
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                val expireDate =
                    ZonedDateTime.now().plus(feature.expiresAfter).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                val nodeId =
                    feature.nodeAddresses["${context.url.protocol.name}://${context.url.host}:${context.url.port}"]
                        ?: throw IllegalArgumentException("Node ${context.url.host} doesn't exist")

                val hash = context.bodyHash()

                context.headers {
                    set("Expires", expireDate)
                    set("Signing-Algorithm", feature.scheme!!.name)
                    set("Client-Public-Key", feature.scheme!!.publicKeyHex)
                    set("Node-Id", nodeId)
                    feature.signedHeaders.forEach { append("Signed-Headers", it) }

                    val content = feature.signedHeaders.asSequence()
                        .map { "$it=${this[it]?.trim() ?: throw IllegalArgumentException("Invalid signed header $it")}" }
                        .joinToString(separator = ";")
                        .let { "${context.method.value}\n${context.url.encodedPath}\n${nodeId}\n${expireDate}\n${it}\n$hash" }

                    set("Request-Signature", feature.scheme!!.sign(content.toByteArray()))
                }
            }
        }

        override fun prepare(block: HttpSecurity.() -> Unit) = HttpSecurity().apply(block).also {
            if (it.scheme == null || it.nodeAddresses.isEmpty() || it.expiresAfter.isZero) {
                throw IllegalStateException("Invalid configuration: $key")
            }
        }

        private suspend fun HttpRequestBuilder.bodyHash(): String {
            val payload = body

            return headers[CONTENT_SHA_256_HEADER] ?: when (payload) {
                is OutgoingContent.NoContent -> ByteArray(0)
                is OutgoingContent.ByteArrayContent -> payload.bytes()
                is OutgoingContent.ReadChannelContent -> payload.readFrom().toByteArray()
                is OutgoingContent.WriteChannelContent -> ByteChannel().also { payload.writeTo(it) }.toByteArray()
                else -> error("No request bytes found: $payload")
            }.sha256()
        }
    }


}




