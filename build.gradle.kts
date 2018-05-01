import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "0.17.3"
    id("org.springframework.boot") version "2.0.1.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version "1.2.41"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.2.41"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"

    java
    eclipse
}

group = "io.pivotal.pal.data"
version = "0.0.1-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://repo.spring.io/milestone")
}

val awaitilityVersion = "3.1.0"
val mockitoKotlinVersion = "2.0.0-alpha03"
val atriumVersion = "0.6.0"
val junit4Version = "4.12"
val junitVintageVersion = "5.1.1"
val junitPlatformVersion = "1.1.1"
val junitJupiterVersion = "5.1.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.hibernate:hibernate-java8")

    runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    testImplementation("ch.tutteli:atrium-cc-en_UK-robstoll")
    testImplementation("ch.tutteli:atrium-verbs")

    // JUnit Jupiter API and TestEngine implementation
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("junit:junit")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine") {
        because("allows JUnit 3 and JUnit 4 tests to run")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
        because("allows tests to run from IDEs that bundle older version of launcher")
    }
}

dependencies {
    constraints {
        compile("org.awaitility:awaitility:$awaitilityVersion")
        compile("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
        compile("ch.tutteli:atrium-cc-en_UK-robstoll:$atriumVersion")
        compile("ch.tutteli:atrium-verbs:$atriumVersion")
        compile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        compile("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
        compile("org.junit.vintage:junit-vintage-engine:$junitVintageVersion")
        compile("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    }
}

// This is a temporary workaround to Gradle adding a second SLF4J implementation
// to the classpath found with the Kotlin DSL conversion.
configurations {
    all { exclude(module = "spring-boot-starter-logging") }
}
