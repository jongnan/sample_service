import com.google.cloud.tools.jib.gradle.JibExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"

    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    id("com.google.cloud.tools.jib") version "3.3.2"
}

group = "com.jongnan"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<JibExtension> {
    val registryUsername = System.getenv("DOCKERHUB_USERNAME")
    val (activeProfile, containerImageName) = getProfileAndImageName(registryUsername)

    from {
        image = "eclipse-temurin:17-jre"
    }

    to {
        image = containerImageName
        tags = setOf("$version", "latest")
        auth {
            username = registryUsername
            password = System.getenv("DOCKERHUB_PASSWORD")
        }
    }

    container {
        // TODO: 서버 스펙에 따라 Xmx/Xms, Initial/Min/MaxRAMFraction 설정
        jvmFlags = listOf(
            "-server",
            "-XX:+UseContainerSupport",
            "-XX:+UseStringDeduplication",
            "-Dserver.port=8080",
            "-Dfile.encoding=UTF-8",
            "-Djava.awt.headless=true",
            "-Dspring.profiles.active=${activeProfile}",
        )
        ports = listOf("8080")
    }
}

fun getProfileAndImageName(registryUsername: String?): Array<String> {
    val containerImageName = "${registryUsername}/${project.name}"
    if (project.hasProperty("release")) {
        return arrayOf("release", containerImageName)
    }
    return arrayOf("dev", "$containerImageName-dev")
}