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

    //JSON
    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")

    // Polkadot
    implementation("emeraldpay.polkaj:polkaj-api-http:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-api-ws:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-api-base:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-schnorrkel:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-scale:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-json-types:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-scale-types:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-common-types:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-ss58:${Versions.polkaj}")
    implementation("emeraldpay.polkaj:polkaj-tx:${Versions.polkaj}")

    //Other
    implementation("com.github.komputing.khex:extensions:${Versions.khex}")
    implementation("com.github.yeeco:schnorrkel-java:${Versions.schnorrkel}")
    implementation("net.openhft:zero-allocation-hashing:0.15")
}