package network.cere.ddc.contract

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.cere.ddc.contract.blockchain.client.JACKSON
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class BucketContractConfig(private val abiFile: Path? = null) {

    private val defaultAbiFile = "${File.separator}ddc_bucket.json"
    private val hashes: MutableMap<String, String> = HashMap()

    suspend fun init() {
        val abiFileJson = withContext(Dispatchers.IO) {
            abiFile?.let { JACKSON.readTree(Files.readAllBytes(it)) }
                ?: javaClass.getResourceAsStream(defaultAbiFile)?.use { JACKSON.readTree(it) }
        }

        abiFileJson?.get("spec")?.get("messages")?.forEach {
            val name = it.withArray<JsonNode>("name")[0].asText()
            val hash = it.get("selector").asText().removePrefix("0x")

            hashes[name] = hash
        }
    }

    fun getMethodHashByName(name: String) = hashes.getValue(name)
}
