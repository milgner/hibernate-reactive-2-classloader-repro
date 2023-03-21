val pf4jVersion: String by project
val vertxVersion: String by project
val kotlinVersion: String by project

plugins {
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa") version "1.8.10"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":api"))
    implementation(kotlin("stdlib"))

    implementation("org.pf4j:pf4j:$pf4jVersion")
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("org.hibernate.reactive:hibernate-reactive-core-jakarta:1.1.9.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("com.linecorp.kotlin-jdsl:hibernate-reactive-kotlin-jdsl-jakarta:2.2.1.RELEASE")
    implementation("io.smallrye.reactive:mutiny-kotlin:2.1.0")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-core:3.2.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("org.kodein.di:kodein-di:7.18.0")
    implementation("io.vertx:vertx-reactive-streams:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")

    kapt("org.pf4j:pf4j:$pf4jVersion")
}
