package network.cere.ddc.contract.options

import network.cere.ddc.core.cid.CidBuilder

class ClientOptions(replication: Int?) {
    val clusterAddress: String | number; // Cluster ID or CDN URL
    val fileOptions: FileOptions? = FileStorageConfig();
    val smartContract: SmartContractOptions? = TESTNET;
    val scheme: SchemeName? | SchemeInterface = "sr25519";
    val cipher: CipherInterface? = new NaclCipher();
    val cidBuilder: CidBuilder? = new CidBuilder();

    constructor(clusterAddress: String | number = "") {
        this.clusterAddress = clusterAddress;
    }

    private val defaultClientOptions = new ClientOptions();
    private val defaultFileOptions = new FileStorageConfig();

    val initDefaultOptions = (options: ClientOptions): ClientOptions => {
        if (!options.clusterAddress && options.clusterAddress != 0) {
            throw new Error(`invalid clusterAddress='${options.clusterAddress}'`)
        }

        options.fileOptions = {
            parallel: options.fileOptions?.parallel || defaultFileOptions.parallel,
            pieceSizeInBytes: options.fileOptions?.pieceSizeInBytes || defaultFileOptions.pieceSizeInBytes,
        }

        return {
            clusterAddress: options.clusterAddress,
            fileOptions: options.fileOptions,
            smartContract: options.smartContract || defaultClientOptions.smartContract,
            scheme: options.scheme || defaultClientOptions.scheme,
            cipher: options.cipher || defaultClientOptions.cipher,
            cidBuilder: options.cidBuilder || defaultClientOptions.cidBuilder
        }
    }

}



