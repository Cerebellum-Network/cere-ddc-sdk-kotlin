package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleWriter
import io.emeraldpay.polkaj.scaletypes.Extrinsic
import network.cere.ddc.contract.model.RawContractCallExtrinsic
import java.io.ByteArrayOutputStream
import java.math.BigInteger

object RawContractCallExtrinsicWriter : ScaleWriter<Extrinsic<RawContractCallExtrinsic>> {

    override fun write(wrt: ScaleCodecWriter, value: Extrinsic<RawContractCallExtrinsic>) {
        val buf = ByteArrayOutputStream()
        val internal = ScaleCodecWriter(buf)
        val type = 132
        internal.writeByte(type)
        internal.write(TransactionInfoWriter, value.tx)
        internal.write(RawContractCallWriter, value.call)
        wrt.writeAsList(buf.toByteArray())
    }

    private object TransactionInfoWriter : ScaleWriter<Extrinsic.TransactionInfo> {
        override fun write(writer: ScaleCodecWriter, value: Extrinsic.TransactionInfo) {
            writer.apply {
                //SC PUB Key prefix
                writeByte(-1)
                writeUint256(value.sender.pubkey)
                writeByte(Extrinsic.SignatureType.SR25519.code)
                writeByteArray(value.signature.value.bytes)
                writeCompact(value.era)
                write(ScaleCodecWriter.COMPACT_BIGINT, BigInteger.valueOf(value.nonce))
                write(ScaleCodecWriter.COMPACT_BIGINT, value.tip.value)
            }

        }
    }

}