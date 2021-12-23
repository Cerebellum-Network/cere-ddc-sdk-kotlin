package network.cere.ddc.core.http

import io.ktor.client.*
import io.ktor.client.engine.java.*

fun defaultHttpClient() = HttpClient(Java) {
    engine { config { version(java.net.http.HttpClient.Version.HTTP_2) } }
}