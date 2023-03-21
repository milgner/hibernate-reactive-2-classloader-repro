val pf4jVersion: String by project
val vertxVersion: String by project
val kotlinVersion: String by project
val pluginsDir: File by rootProject.extra

plugins {
    kotlin("jvm")
    application

    id("org.jetbrains.kotlin.plugin.jpa") version "1.8.10"
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
    implementation(project(":api"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("com.graphql-java:graphql-java-extended-scalars:20.0")
    implementation("org.postgresql:postgresql:42.5.4") {
        because("Database connection \uD83E\uDD21")
    }

    implementation("org.kodein.di:kodein-di:7.18.0") {
        because("Dependency Injection!")
    }
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("org.hibernate.reactive:hibernate-reactive-core:2.0.0.Alpha2")
    implementation("com.linecorp.kotlin-jdsl:hibernate-reactive-kotlin-jdsl-jakarta:2.2.1.RELEASE") {
        because("Smooth type-safe querying for Hibernate")
    }
    implementation("io.smallrye.reactive:mutiny-kotlin:2.1.0") {
        because("Mutiny is preferred to CompletionStage by JDSL")
    }
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-core:3.2.0")
    implementation("io.vertx:vertx-core:$vertxVersion") { because("\uD83D\uDE80") }
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-web-graphql:$vertxVersion")
    implementation("org.ow2.asm:asm:9.4") {
        because("Required by PF4J for fancy optional dependencies")
    }
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("org.slf4j:slf4j-log4j12:2.0.6")
    implementation("org.pf4j:pf4j:$pf4jVersion")
    implementation("io.vertx:vertx-reactive-streams:$vertxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") {
        because("Java Time APIs don't have serialization and other comforts")
    }
    implementation("info.faljse:SDNotify:1.3") {
        because("Let systemd know when the process is ready")
    }
}

application {
    mainClass.set("com.marcusilgner.BootKt")
}

tasks.named<JavaExec>("run") {
    dependsOn(":plugins:assemblePlugins")

    // Make properties from the CLI available to the started process
    systemProperties(System.getProperties().toMap() as Map<String, String>)
    systemProperty("pf4j.pluginsDir", pluginsDir.absolutePath)
}
