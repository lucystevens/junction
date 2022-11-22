import com.avast.gradle.dockercompose.ComposeSettings

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"

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

    create("acceptanceTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val acceptanceTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val ktormVersion = "3.5.0"
val koinVersion= "3.2.2"
val acme4jVersion = "2.14"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.insert-koin:koin-core:$koinVersion")

    // ktorm for database connections
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-support-sqlite:$ktormVersion")
    implementation("org.ktorm:ktorm-jackson:$ktormVersion")
    implementation("org.xerial:sqlite-jdbc:3.39.3.0")

    // undertow + kotlinx
    implementation("io.undertow:undertow-core:2.3.0.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // acme4j
    implementation("org.shredzone.acme4j:acme4j-client:$acme4jVersion")
    implementation("org.shredzone.acme4j:acme4j-utils:$acme4jVersion")

    // logback for logging
    implementation("ch.qos.logback:logback-classic:1.4.5")


    // testing
    //testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // javalin + okhttp for mock/test server
    testImplementation("io.javalin:javalin:5.1.3")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation("com.squareup.okhttp3:okhttp-tls:4.10.0")
    testImplementation("com.google.code.gson:gson:2.10")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.72")
}

application {
    mainClass.set("uk.co.lucystevens.junction.LauncherKt")
}

/**
 *  Tasks
 */
configure<ComposeSettings> {
    startedServices.set(listOf("junction", "pebble", "integration-test"))
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

val acceptanceTest = task<Test>("acceptanceTest") {
    useJUnitPlatform()
    description = "Runs acceptance tests."
    group = "verification"

    testClassesDirs = sourceSets["acceptanceTest"].output.classesDirs
    classpath = sourceSets["acceptanceTest"].runtimeClasspath
    outputs.upToDateWhen { false }
}

val integrationTestInternal = task<Test>("integrationTestInternal") {
    useJUnitPlatform()

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
}

// TODO this doesn't actually work because the containers are shut down as soon as they are started
val integrationTest = task("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    mustRunAfter(tasks.composeUp)
}

dockerCompose.isRequiredBy(integrationTest)
