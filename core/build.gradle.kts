dependencies {
    // Crypto
    implementation("org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}")
    implementation("com.github.yeeco:schnorrkel-java:${Versions.schnorrkel}")
    implementation("org.purejava:tweetnacl-java:${Versions.tweetnacl}")
    implementation("org.apache.tuweni:tuweni-crypto:${Versions.tuweni}")
    implementation("net.java.dev.jna:jna:${Versions.jna}")

    implementation("com.github.komputing.khex:core:${Versions.khex}")
    implementation("com.github.komputing.khex:extensions:${Versions.khex}")
    implementation("com.github.komputing.kethereum:model:${Versions.kethereum}")
    implementation("com.github.komputing.kethereum:crypto:${Versions.kethereum}")
    implementation("com.github.komputing.kethereum:crypto_impl_bouncycastle:${Versions.kethereum}")
    implementation("com.github.komputing.kethereum:extensions_kotlin:${Versions.kethereum}")
    implementation("com.github.komputing.kethereum:keccak_shortcut:${Versions.kethereum}")

    // CID
    implementation("com.github.ipld:java-cid:${Versions.cid}")
}