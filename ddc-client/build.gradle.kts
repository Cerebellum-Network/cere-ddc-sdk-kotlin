plugins {
    id("java")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
    ivy("https://github.com") {
        patternLayout {
            artifact("[organisation]/releases/download/v[revision]/[module]-[revision].jar")
            setM2compatible(true)
        }
        metadataSources { artifact() }
    }
}

dependencies {

    //JSON
    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")

    implementation(project(":core"))
    implementation(project(":smart-contract"))
    implementation(project(":content-addressable-storage"))
    implementation(project(":key-value-storage"))
    implementation(project(":file-storage"))

    implementation("org.purejava:tweetnacl-java:${Versions.tweetnacl}")
    implementation("org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}")
    implementation("com.github.komputing.khex:extensions:${Versions.khex}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}