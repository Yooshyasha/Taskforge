plugins {
    kotlin("jvm") version "2.3.10" apply false

    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false

    kotlin("plugin.spring") version "2.3.10" apply false
    kotlin("plugin.jpa") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
}