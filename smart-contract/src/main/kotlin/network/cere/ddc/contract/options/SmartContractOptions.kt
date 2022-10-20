package network.cere.ddc.contract.options

import network.cere.ddc.contract.abi.ddcBucketAbi
import java.io.File

enum class SmartContractOptions(var rpcUrl: String, var contractAddress: String, var abi: File) {
    MAINNET("NOT_DEPLOYED_YET", "NOT_DEPLOYED_YET", ddcBucketAbi),
    TESTNET("wss://rpc.testnet.cere.network/ws", "5DTZfAcmZctJodfa4W88BW5QXVBxT4v7UEax91HZCArTih6U", ddcBucketAbi),
    DEVNET("wss://rpc.devnet.cere.network/ws", "5GqwX528CHg1jAGuRsiwDwBVXruUvnPeLkEcki4YFbigfKsC", ddcBucketAbi);
}