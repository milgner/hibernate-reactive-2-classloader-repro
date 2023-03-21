val pluginsDir by extra { file("$buildDir/plugins") }

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.10"))
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
}

tasks.build {
    dependsOn("plugins:assemblePlugins")
}
