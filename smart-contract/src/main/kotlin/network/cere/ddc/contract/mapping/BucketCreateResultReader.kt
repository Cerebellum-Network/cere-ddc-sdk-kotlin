package network.cere.ddc.contract.mapping

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import network.cere.ddc.contract.model.BucketCreateResult

object BucketCreateResultReader : ScaleReader<BucketCreateResult> {

    override fun read(reader: ScaleCodecReader): BucketCreateResult {
        return BucketCreateResult(
            bucketId = reader.readUint32(),
            owner = reader.read(AccountIdReader)
        )
    }
}