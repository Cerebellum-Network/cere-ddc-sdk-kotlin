package network.cere.ddc.contract.client

import io.emeraldpay.polkaj.scaletypes.ExtrinsicCall
import io.emeraldpay.polkaj.types.Address
import java.math.BigInteger

data class RawContractCallExtrinsic(
    val data: ByteArray,
    val contractAddress: Address,
    val gasLimit: BigInteger,
    val value: BigInteger = BigInteger.ZERO
) : ExtrinsicCall()