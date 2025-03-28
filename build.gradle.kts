import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val h2_version: String by project
val kotlin_version: String by project
val kotlinx_html_version: String by project
val logback_version: String by project
val postgres_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
}

group = "trotech"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += listOf("-Xdebug", "-Xno-optimize")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-apache")
    implementation("io.ktor:ktor-server-sse")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinx_html_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.github.flaxoos:ktor-server-kafka:2.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-network-tls")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.1.2")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-core:2.1.2")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-redis:2.1.2")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-mongodb:2.1.2")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-jdbc:2.1.2")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.hibernate:hibernate-core:6.4.2.Final")
    implementation("org.hibernate:hibernate-entitymanager:5.6.15.Final")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("org.jetbrains.kotlin.plugin.lombok:org.jetbrains.kotlin.plugin.lombok.gradle.plugin:1.9.24")
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-cors")
    implementation("redis.clients:jedis:5.0.2")
}
