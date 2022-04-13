package network.cere.ddc.contract.mapping.response

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleReader
import io.emeraldpay.polkaj.scale.reader.ListReader
import network.cere.ddc.contract.model.response.ResultList

class ResultListReader<T>(reader: ScaleReader<T>) : ScaleReader<ResultList<T>> {

    private val listReader = ListReader(reader)

    override fun read(reader: ScaleCodecReader) = ResultList<T>(
        result = reader.read(listReader),
        totalLength = reader.readUint32()
    )
}