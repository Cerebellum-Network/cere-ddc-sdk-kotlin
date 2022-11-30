plugins {
    kotlin("jvm") version Versions.kotlin

    id("com.avast.gradle.docker-compose") version Versions.docker
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    group = "com.github.cerebellum-network.cere-ddc-sdk-kotlin"

    apply(plugin = "kotlin")
    apply(plugin = "maven")
    apply(plugin = "idea")
    apply(plugin = "docker-compose")

    afterEvaluate {
        dependencies {
            implementation(kotlin("stdlib"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

            // HTTP
            implementation("io.ktor:ktor-client-core:${Versions.ktor}")

            // Logging
            implementation("org.slf4j:slf4j-api:${Versions.slf4j}")

            // Test
            testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}")
            testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
            testImplementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
            testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockito}")
            testImplementation("io.ktor:ktor-client-mock:${Versions.ktor}")
            testImplementation("io.ktor:ktor-client-java:${Versions.ktor}")
        }
    }

    val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
    val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

    compileKotlin.kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        javaParameters = true
    }
    compileTestKotlin.kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }


    dockerCompose {
        isRequiredBy(tasks.compileTestKotlin)
        useComposeFiles = listOf("${rootProject.buildDir}/../docker-compose/docker-compose.yml")
        noRecreate = true
    }
}



