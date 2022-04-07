package network.cere.ddc.contract.blockchain.mapping

import io.emeraldpay.polkaj.scale.ScaleReader

interface IndexedScaleReader<T> : ScaleReader<T> {
    abstract val moduleId: Int
    abstract val actionId: Int
}