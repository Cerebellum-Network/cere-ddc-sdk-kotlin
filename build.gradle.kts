plugins {
    kotlin("jvm") version Versions.kotlin

    id("com.avast.gradle.docker-compose") version Versions.docker

    maven
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://jitpack.io")
}

group = "com.github.cerebellum-network"

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    apply(plugin = "kotlin")
    apply(plugin = "docker-compose")

    tasks.withType<Test> {
        dependsOn(tasks.composeUp)
        finalizedBy(tasks.composeDown)
        useJUnitPlatform()
    }

    dockerCompose {
        useComposeFiles = listOf("${ rootProject.buildDir }/../docker-compose/docker-compose.yml")
    }

    afterEvaluate {
        dependencies {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")

            // HTTP
            "implementation"("io.ktor:ktor-client-core:${Versions.ktor}")
            "implementation"("io.ktor:ktor-client-java:${Versions.ktor}")

            // Logging
            "implementation"("org.slf4j:slf4j-api:${Versions.slf4j}")

            // Test
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}")
            "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
            "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
            "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
            "testImplementation"("org.assertj:assertj-core:${Versions.assertj}")
            "testImplementation"("org.mockito.kotlin:mockito-kotlin:${Versions.mockito}")
            "testImplementation"("com.github.tomakehurst:wiremock:${Versions.wiremock}")
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
}



