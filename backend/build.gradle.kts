plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.pantheon"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.hibernate.orm:hibernate-community-dialects")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("io.vavr:vavr:0.11.0")

    implementation("org.flywaydb:flyway-core")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("org.xerial:sqlite-jdbc")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val exclusionList = listOf(
    "**/dto/**",
    "**/model/**",
    "**/repository/**",
    "**/config/**",            // Usually config is excluded too
    "**/BackendApplication*",  // The main class
    "**/SsePubSub*"            // Exclude until you are ready to test it
)

val filteredClassFiles = sourceSets.main.get().output.asFileTree.matching {
    exclude(exclusionList)
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview", "-XX:+EnableDynamicAgentLoading")
    finalizedBy("jacocoTestReport")
}

tasks.register("testThenBuild") {
    group = "build"
    description = "Runs all tests, and if successful, builds the project."
    dependsOn("build")
}

tasks.register("testThenRun") {
    group = "application"
    description = "Runs all tests, and if successful, starts the application."

    // We want both tasks to happen
    dependsOn("test", "bootRun")
}

tasks.named("bootRun") {
    mustRunAfter("test")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(filteredClassFiles)
}

tasks.jacocoTestCoverageVerification {

    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(filteredClassFiles)

    violationRules {
        rule {
            element = "BUNDLE"

            limit {
                counter = "METHOD"
                value = "COVEREDRATIO"
                minimum = 0.80.toBigDecimal() // Replace with your 'x'
            }

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.60.toBigDecimal() // Replace with your 'y'
            }
        }
    }
}

tasks.register("testWithCoverage") {
    group = "verification"
    description = "Runs tests, generates report, and fails if coverage is too low."

    // This chain ensures: test -> jacocoTestReport -> jacocoTestCoverageVerification
    dependsOn(tasks.jacocoTestCoverageVerification)
}

