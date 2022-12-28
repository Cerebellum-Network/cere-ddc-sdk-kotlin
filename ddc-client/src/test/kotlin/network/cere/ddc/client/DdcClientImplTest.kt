package network.cere.ddc.client

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import kotlinx.coroutines.runBlocking
import network.cere.ddc.`ca-storage`.domain.Piece
import network.cere.ddc.`ca-storage`.domain.Tag
import network.cere.ddc.client.options.ReadOptions
import network.cere.ddc.client.options.StoreOptions
import network.cere.ddc.contract.options.ClientOptions
import org.junit.jupiter.api.Test

internal class DdcClientImplTest {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val cdnNodeUrl = "http://localhost:8080"
    private val clientOptions = ClientOptions(cdnNodeUrl)
    private val balance = 1L
    private val resource = 1L
    private val clusterId = 2L
    private val bucketId = 1L

    @Test
    fun storeAndRead() {
        runBlocking {
            val ddcClient = DdcClientImpl.buildAndConnect(clientOptions, privateKey, null)
            val tags = listOf(Tag(key = "Search", value = "test"))
            val piece = Piece(data = "Hello world!".toByteArray(), tags = tags)
            val storeOptions = StoreOptions("/", false)
            val readOptions = ReadOptions("/", false)
            //when
            val ddcUri = ddcClient.store(bucketId, piece, storeOptions)
            //then
            val result = ddcClient.read(ddcUri, readOptions)
            result shouldBeEqualToComparingFields piece
        }
    }

    @Test
    fun storeAndReadEncrypted() {
        runBlocking {
            val ddcClient = DdcClientImpl.buildAndConnect(clientOptions, privateKey, null)
            val tags = listOf(Tag(key = "Search", value = "test"))
            val piece = Piece(data = "Hello world!".toByteArray(), tags = tags)
            val storeOptions = StoreOptions("1/", true)
            val readOptions = ReadOptions("1", true)
            //when
            val ddcUri = ddcClient.store(bucketId, piece, storeOptions)
            //then
            val result = ddcClient.read(ddcUri, readOptions)
            result shouldBeEqualToComparingFields piece
        }
    }
}