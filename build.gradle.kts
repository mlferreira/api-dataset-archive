import org.springframework.boot.gradle.tasks.run.BootRun as BootRun
import org.springframework.boot.gradle.tasks.bundling.BootJar as BootJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile as KotlinCompile
import org.gradle.kotlin.dsl.springBoot


plugins {
    application
    java
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE" // apply false
    id("java-library")
    id ("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    kotlin("plugin.jpa") version "1.3.61"
    kotlin("plugin.allopen") version "1.3.61"
}

apply(plugin = "io.spring.dependency-management")

version = "0.0.1"
group = "org.xr4good.datasetarchive"

application {
    mainClassName = "org.xr4good.datasetarchive.DatasetArchiveApplicationKt"
}

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
    mavenLocal()
    val repoList = listOf(
            "http://repo.spring.io/milestone",
            "http://repo.spring.io/libs-milestone",
            "http://repo.spring.io/snapshot"
    )
    repoList.forEach { maven(it) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {

    // AWS
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.774"))
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.774")

    // KOTLIN
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // SPRING WEB
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }

    // SPRING BOOT STARTERS
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // MYSQL
    implementation("mysql:mysql-connector-java")
    implementation("org.hibernate:hibernate-core")

    // JDBC
    implementation("org.springframework.data:spring-data-jdbc")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // SWAGGER
    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")

    // CACHE (REDIS)
    implementation("org.springframework.data:spring-data-redis")
    implementation("redis.clients:jedis:3.2.0")

    // ACL
    implementation("org.springframework.security:spring-security-acl")

    // TOKEN
    implementation("io.jsonwebtoken:jjwt:0.9.1")

    // SPRING SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-web")

    // DEBBUGING TOOLS
    developmentOnly("org.springframework.boot:spring-boot-devtools")

}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict -XX:-UseCompressedOops")
        jvmTarget = "1.8"
    }
}

tasks.getByName<BootJar>("bootJar") {
    classifier = "boot"
    mainClassName = "org.xr4good.datasetarchive.DatasetArchiveApplicationKt"
    archiveClassifier
    launchScript()
    manifest {
        attributes("Start-Class" to "org.xr4good.datasetarchive.DatasetArchiveApplicationKt")
    }
}

tasks.getByName<BootRun>("bootRun") {
    main = "org.xr4good.datasetarchive.DatasetArchiveApplicationKt"
}

