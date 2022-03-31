package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.UInt128Reader
import java.math.BigInteger

object UInt64Reader : ScaleReader<BigInteger> {

    override fun read(rdr: ScaleCodecReader): BigInteger {
        val value = rdr.readByteArray(8)
        UInt128Reader.reverse(value)
        return BigInteger(1, value)
    }
}