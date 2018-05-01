buildscript {
    ext {
        kotlinVersion = '1.2.40'
        springBootVersion = '2.0.1.RELEASE'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${kotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-noarg:${kotlinVersion}")
    }
}

apply plugin: 'kotlin'
apply plugin: 'kotlin-spring'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
apply plugin: 'kotlin-jpa'

group = 'io.pivotal.pal.data'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8
compileKotlin {
    kotlinOptions {
        freeCompilerArgs = ["-Xjsr305=strict"]
        jvmTarget = "1.8"
    }
}
compileTestKotlin {
    kotlinOptions {
        freeCompilerArgs = ["-Xjsr305=strict"]
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven { url "https://repo.spring.io/milestone" }
}


ext {
    awaitilityVersion = '3.1.0'
    mockitoKotlinVersion = '2.0.0-alpha03'
    atriumVersion = '0.6.0'
    junit4Version = '4.12'
    junitVintageVersion = '5.1.1'
    junitPlatformVersion = '1.1.1'
    junitJupiterVersion = '5.1.1'
}

dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-jpa')
    implementation('com.fasterxml.jackson.module:jackson-module-kotlin')
    implementation('org.springframework.kafka:spring-kafka')
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation('org.hibernate:hibernate-java8')

    runtimeOnly('org.hsqldb:hsqldb')
    runtimeOnly('com.h2database:h2')
    runtimeOnly('org.postgresql:postgresql')
    runtimeOnly('org.springframework.boot:spring-boot-devtools')

    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
    testImplementation('org.awaitility:awaitility')
    testImplementation('com.nhaarman.mockitokotlin2:mockito-kotlin')
    testImplementation('ch.tutteli:atrium-cc-en_UK-robstoll')
    testImplementation('ch.tutteli:atrium-verbs')

    // JUnit Jupiter API and TestEngine implementation
    testImplementation('org.junit.jupiter:junit-jupiter-api')
    testRuntimeOnly('org.junit.jupiter:junit-jupiter-engine')

    testImplementation('junit:junit')
    testRuntimeOnly('org.junit.vintage:junit-vintage-engine') {
        because 'allows JUnit 3 and JUnit 4 tests to run'
    }

    testRuntimeOnly('org.junit.platform:junit-platform-launcher') {
        because 'allows tests to run from IDEs that bundle older version of launcher'
    }
}

dependencies {
    constraints {
        compile "org.awaitility:awaitility:${awaitilityVersion}"
        compile "com.nhaarman.mockitokotlin2:mockito-kotlin:${mockitoKotlinVersion}"
        compile "ch.tutteli:atrium-cc-en_UK-robstoll:${atriumVersion}"
        compile "ch.tutteli:atrium-verbs:${atriumVersion}"
        compile "org.junit.jupiter:junit-jupiter-api:${junitJupiterVersion}"
        compile "org.junit.jupiter:junit-jupiter-engine:${junitJupiterVersion}"
        compile "org.junit.vintage:junit-vintage-engine:${junitVintageVersion}"
        compile "org.junit.platform:junit-platform-launcher:${junitPlatformVersion}"
    }
}
