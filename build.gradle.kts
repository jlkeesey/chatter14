/*
 * Copyright 2022 James Keesey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

@file:Suppress("SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
    id("net.researchgate.release") version "2.8.1"
    id("edu.sc.seis.launch4j") version "2.5.3"
}

group = "pub.carkeys.logparse"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("cc.ekblad:4koma:1.0.1")
    implementation("com.github.ajalt.clikt:clikt:3.4.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")
    testImplementation("io.kotest:kotest-property:5.3.0")
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("com.github.erikhuizinga:mockk-junit5:2.0.0")
}

val resourceGenerationDir by extra("${buildDir}/generated_src/main/resources")
val resourceVersionProperties by extra("${resourceGenerationDir}/version.properties")

tasks {
    val versionProperties by registering(WriteProperties::class) {
        description = "Generated version property file"
        comment = "Generated version property file"
        outputFile = file(resourceVersionProperties)
        encoding = "UTF-8"
        property("${project.name}.version", project.version)
    }

    processResources {
        from(versionProperties)
    }
}

tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
    outfile = "${rootProject.name}.exe"
    mainClassName = "pub.carkeys.logparse.MainKt"
    icon = "$projectDir/icons/Iconka-Cat-Shadows-Cat-shadow.ico"
    jvmOptions = setOf("-Dbuild.version=${version}")
    productName = "LogParse"
    bundledJrePath = "C:/Program Files/Java/jdk-18.0.1.1"
    jreMinVersion = "1.8.0"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

application {
    mainClass.set("pub.carkeys.logparse.MainKt")
}