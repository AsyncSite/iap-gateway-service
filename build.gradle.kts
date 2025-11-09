plugins {
    id("java")
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.asyncsite"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    // GitHub Packages - common-tracer
    maven {
        name = "GitHubPackages-common-tracer"
        url = uri("https://maven.pkg.github.com/asyncsite/common-tracer")
        credentials {
            username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR") ?: System.getenv("USERNAME")
            password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN") ?: System.getenv("TOKEN")
        }
    }
}

ext {
    set("springCloudVersion", "2025.0.0")
}

dependencies {
    // Core Platform
    implementation("com.asyncsite.coreplatform:common:1.1.0-SNAPSHOT")

    // Tracing
    implementation("com.asyncsite:common-tracer:0.1.0-SNAPSHOT")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Google Cloud Pub/Sub
    implementation("com.google.cloud:google-cloud-pubsub:1.125.11")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.15.0")

    // JWT (Apple App Store Server Notifications V2)
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")

    // WebFlux (HTTP Client for Apple public key download)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Springdoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}