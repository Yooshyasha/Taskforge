plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.yooshyasha"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // Source: https://mvnrepository.com/artifact/org.meeuw.i18n/i18n-iso-639
    implementation("org.meeuw.i18n:i18n-iso-639:4.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}