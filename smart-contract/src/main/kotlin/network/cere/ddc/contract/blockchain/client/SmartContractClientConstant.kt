package network.cere.ddc.contract.blockchain.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import java.math.BigInteger


internal const val CONTRACT_CALL_READ_COMMAND = "contracts_call"
internal const val STATE_GET_STORAGE_READ_COMMAND = "state_getStorage"
internal const val SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND = "author_submitAndWatchExtrinsic"
internal const val UNSUBSCRIBE_TRANSACTION_EVENTS_COMMAND = "author_unwatchExtrinsic"

internal const val SYSTEM_EVENTS_KEY_STATE_STORAGE =
    "0x26aa394eea5630e07c48ae0c9558cef780d41e5e16056765bc8461851072c9d7"

internal const val MAX_GAS_LIMIT_READ = 4999999999999L
internal val MAX_GAS_LIMIT_TRANSACTION: BigInteger = BigInteger.valueOf(1280000000000)

internal val JACKSON = jacksonObjectMapper().registerModule(PolkadotModule())

