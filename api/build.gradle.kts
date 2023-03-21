val pf4jVersion: String by project
val vertxVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.10"
}

// not strictly necessary, but better safe than sorry
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// not necessary for Gradle 8, but better to have it explicit
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    compileOnly("io.vertx:vertx-core:$vertxVersion")
    compileOnly("io.vertx:vertx-lang-kotlin:$vertxVersion")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    compileOnly("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    compileOnly("io.vertx:vertx-reactive-streams:$vertxVersion")
    compileOnly("org.pf4j:pf4j:$pf4jVersion")
    compileOnly("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    compileOnly("org.hibernate.reactive:hibernate-reactive-core-jakarta:1.1.9.Final")
    compileOnly("org.kodein.di:kodein-di:7.18.0")
}
