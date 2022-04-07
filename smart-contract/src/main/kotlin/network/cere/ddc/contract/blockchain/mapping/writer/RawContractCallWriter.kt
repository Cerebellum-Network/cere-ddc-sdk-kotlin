package network.cere.ddc.contract.blockchain.mapping.writer

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleWriter
import network.cere.ddc.contract.client.RawContractCallExtrinsic


object RawContractCallWriter : ScaleWriter<RawContractCallExtrinsic> {
    override fun write(writer: ScaleCodecWriter, value: RawContractCallExtrinsic) {
        writer.apply {
            //call_index - Polkaj doesn't support Metadata v12, so we need to add it manually
            writeByteArray(byteArrayOf(18, 2))

            //SC PUB Key with prefix
            writeByte(-1)
            writeUint256(value.contractAddress.pubkey)

            write(ScaleCodecWriter.COMPACT_BIGINT, value.value)
            write(ScaleCodecWriter.COMPACT_BIGINT, value.gasLimit)
            writeAsList(value.data)
        }
    }
}