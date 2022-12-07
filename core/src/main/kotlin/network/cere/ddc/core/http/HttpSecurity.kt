package network.cere.ddc.core.http

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.signature.Scheme
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Security plugin for [HttpClient].
 *
 * Adds to HTTP request security values and signatures to headers.
 * Headers: 'Expires', 'Signing-Algorithm', 'Client-Public-Key', 'Node-Id', 'Request-Signature', 'Signed-Headers'
 * 'Content-SHA256' (PUT method) and 'Content-Signature' (PUT method).
 */
class HttpSecurity {
    /** Used for signing request */
    var scheme: Scheme? = null

    /** Used for setting 'Expires' header */
    var expiresAfter: Duration = Duration.ZERO

    /**
     * [Map] of node addresses for setting 'Node-Id' by http address. Have to be extended for new node adding in runtime.
     */
    var nodeAddresses = mapOf<String, String>()

    /** Custom headers for adding to request signature (header 'Request-Signature') */
    var signedHeaders: List<String> = listOf()

    companion object Feature : HttpClientFeature<HttpSecurity, HttpSecurity> {

        override val key: AttributeKey<HttpSecurity> = AttributeKey("HttpSecurityFeature")

        override fun install(feature: HttpSecurity, scope: HttpClient) {
            val phase = PipelinePhase("SecurityHeaders")
            scope.sendPipeline.insertPhaseAfter(HttpSendPipeline.State, phase)

            scope.sendPipeline.intercept(phase) {
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

                    if (context.method == HttpMethod.Put) {
                        set("Content-SHA256", hash)
                        set("Content-Signature", feature.scheme!!.sign(hash.toByteArray()))
                    }

                    val signedHeaders = feature.signedHeaders.takeIf(List<String>::isNotEmpty)
                        ?.also { appendAll("Signed-Headers", it) }
                        ?.let {
                            feature.signedHeaders.asSequence()
                                .map { "$it=${this[it]?.trim() ?: throw IllegalArgumentException("Invalid signed header $it")}" }
                                .joinToString(separator = ";")
                        } ?: ""

                    val content =
                        "${context.method.value}\n${context.url.encodedPath}\n${nodeId}\n${expireDate}\n${signedHeaders}\n$hash"

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

            return when (payload) {
                is OutgoingContent.NoContent -> ByteArray(0)
                is OutgoingContent.ByteArrayContent -> payload.bytes()
                is OutgoingContent.ReadChannelContent -> payload.readFrom().toByteArray()
                is OutgoingContent.WriteChannelContent -> ByteChannel().also { payload.writeTo(it) }.toByteArray()
                else -> error("No request bytes found: $payload")
            }.sha256()
        }
    }


}




