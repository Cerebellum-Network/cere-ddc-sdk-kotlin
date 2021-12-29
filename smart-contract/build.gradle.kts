dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")

    // Polkadot
    implementation("io.emeraldpay.polkaj:polkaj-api-http:0.3.0")
    implementation("io.emeraldpay.polkaj:polkaj-api-base:0.3.0")
    implementation("io.emeraldpay.polkaj:polkaj-schnorrkel:0.3.0")
    implementation("io.emeraldpay.polkaj:polkaj-scale:0.3.0")
    implementation("io.emeraldpay.polkaj:polkaj-json-types:0.3.0")
    implementation("io.emeraldpay.polkaj:polkaj-common-types:0.3.0")
}