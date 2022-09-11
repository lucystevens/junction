import com.avast.gradle.dockercompose.ComposeSettings

plugins {
    kotlin("jvm") version "1.6.10"

    application
    `maven-publish`

    id("com.avast.gradle.docker-compose") version "0.14.9"
    id("uk.co.lukestevens.plugins.release-helper") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "uk.co.lucystevens"
version = "0.0.1"

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val koinVersion= "3.1.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.insert-koin:koin-core:$koinVersion")

    // undertow
    implementation("io.undertow:undertow-core:2.2.19.Final")

    // logback for logging
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.github.maricn:logback-slack-appender:1.6.1")

    // testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.3")
}

application {
    mainClass.set("uk.co.lucystevens.LauncherKt")
}

/**
 *  Tasks
 */
configure<ComposeSettings> {
    startedServices.set(listOf("local-db"))
    forceRecreate.set(true)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
}

val integrationTest = task<Test>("integrationTest") {
    useJUnitPlatform()
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
    mustRunAfter(tasks.composeUp)
}

dockerCompose.isRequiredBy(integrationTest)
