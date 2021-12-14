buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    }
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    apply(plugin = "kotlin")

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    afterEvaluate {
        dependencies {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
            "implementation"("io.ktor:ktor-client-core:${Versions.ktor}")

            // Test
            "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
        }
    }

    val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
    val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

    compileKotlin.kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    compileTestKotlin.kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}