repositories {
    // For Polkaj dependency
    ivy("https://github.com") {
        patternLayout {
            artifact("[organisation]/releases/download/v[revision]/[module]-[revision].jar")
            setM2compatible(true)
        }
        metadataSources { artifact() }
    }
}

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")

    // Polkadot
    implementation("emeraldpay.polkaj:polkaj-api-http:0.3.0")
    implementation("emeraldpay.polkaj:polkaj-api-base:0.3.0")
    implementation("emeraldpay.polkaj:polkaj-schnorrkel:0.3.0")
    implementation("emeraldpay.polkaj:polkaj-scale:0.3.0")
    implementation("emeraldpay.polkaj:polkaj-json-types:0.3.0")
    implementation("emeraldpay.polkaj:polkaj-common-types:0.3.0")
}