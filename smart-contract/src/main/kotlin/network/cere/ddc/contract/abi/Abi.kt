package network.cere.ddc.contract.abi

import java.io.File

val ddcBucketAbi = File("ddc_bucket.json").inputStream().readBytes().toString(Charsets.UTF_8) //TODO update file?