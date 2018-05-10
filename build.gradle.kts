import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.2.41"
    val kotlinDslVersion = "0.17.3"
    val springBootVersion = "2.0.2.RELEASE"
    val springDependencyManagementPluginVersion = "1.0.5.RELEASE"

    id("org.gradle.kotlin.kotlin-dsl") version kotlinDslVersion
    id("org.springframework.boot") version springBootVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("io.spring.dependency-management") version springDependencyManagementPluginVersion

    kotlin("jvm") version kotlinVersion
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

//This is necessary to make the version accessible in other places
val kotlinVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "kotlin-gradle-plugin" }?.moduleVersion
}

val awaitilityVersion: String by project
val mockitoKotlinVersion: String by project
val atriumVersion: String by project
val junit4Version: String by project
val junitVintageVersion: String by project
val junitPlatformVersion: String by project
val junitJupiterVersion: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.kafka:spring-kafka")
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

    constraints {
        // TODO: figure out how to set Kotlin version properly
        embeddedKotlin("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        embeddedKotlin("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        testCompileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        testCompileOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        testImplementation("org.awaitility:awaitility:$awaitilityVersion")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
        testImplementation("ch.tutteli:atrium-cc-en_UK-robstoll:$atriumVersion")
        testImplementation("ch.tutteli:atrium-verbs:$atriumVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
        testImplementation("org.junit.vintage:junit-vintage-engine:$junitVintageVersion")
        testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    }

}

// This is a temporary workaround to Gradle adding a second SLF4J implementation
// to the classpath found with the Kotlin DSL conversion.
configurations {
    all { exclude(module = "spring-boot-starter-logging") }
}
