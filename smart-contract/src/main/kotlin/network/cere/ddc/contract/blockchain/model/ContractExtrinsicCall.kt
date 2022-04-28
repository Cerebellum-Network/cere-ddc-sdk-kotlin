package network.cere.ddc.contract.blockchain.model

import io.emeraldpay.polkaj.scaletypes.ExtrinsicCall
import io.emeraldpay.polkaj.types.Address
import java.math.BigInteger

data class ContractExtrinsicCall(
    val data: ByteArray,
    val contractAddress: Address,
    val gasLimit: BigInteger,
    val value: BigInteger
) : ExtrinsicCall()