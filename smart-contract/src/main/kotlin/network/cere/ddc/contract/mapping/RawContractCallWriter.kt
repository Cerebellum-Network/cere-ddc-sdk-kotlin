package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleWriter
import network.cere.ddc.contract.model.RawContractCallExtrinsic


object RawContractCallWriter : ScaleWriter<RawContractCallExtrinsic> {
    override fun write(writer: ScaleCodecWriter, value: RawContractCallExtrinsic) {
        writer.apply {
            //call_index
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