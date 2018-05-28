import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.2.41"
    id("org.gradle.kotlin.kotlin-dsl") version "0.17.3"
    id("org.springframework.boot") version "2.0.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
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

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://repo.spring.io/milestone")
}

//This is necessary to make the version accessible in other places
val kotlinVersion: String? by extra {
    buildscript.configurations["classpath"]
        .resolvedConfiguration.firstLevelModuleDependencies
        .find { it.moduleName == "kotlin-gradle-plugin" }?.moduleVersion
}

val awaitilityVersion: String by project
val mockitoKotlinVersion: String by project
val atriumVersion: String by project
val junitPlatformVersion: String by project
val junitJupiterVersion: String by project

dependencies {
    embeddedKotlin(kotlin("stdlib-jdk8", kotlinVersion))
    embeddedKotlin(kotlin("reflect", kotlinVersion))

    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.hibernate:hibernate-java8")

    runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    testImplementation("ch.tutteli:atrium-cc-en_UK-robstoll")
    testImplementation("ch.tutteli:atrium-verbs")

    // JUnit Jupiter API and TestEngine implementation
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
        because("allows tests to run from IDEs that bundle older version of launcher")
    }

    constraints {
        embeddedKotlin("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        embeddedKotlin("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        testCompileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        testCompileOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        testImplementation("org.awaitility:awaitility:$awaitilityVersion")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
        testImplementation("ch.tutteli:atrium-cc-en_UK-robstoll:$atriumVersion")
        testImplementation("ch.tutteli:atrium-verbs:$atriumVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
        testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    }

}

// This is a temporary workaround to Gradle adding a second SLF4J implementation
// to the classpath found with the Kotlin DSL conversion.
configurations {
    all { exclude(module = "spring-boot-starter-logging") }
}
