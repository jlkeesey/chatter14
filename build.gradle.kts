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
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.1")

    testImplementation(kotlin("test"))
}

//launch4j {
//    mainClassName = "pub.carkeys.logparse.MainKt"
//    icon = "${projectDir}/icons/logparse.ico"
//}

tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
    outfile = "${rootProject.name}.exe"
    mainClassName = "pub.carkeys.logparse.MainKt"
    icon = "$projectDir/icons/${rootProject.name}.ico"
    productName = "LogParse"
    bundledJrePath = "C:/Program Files/Java/jdk-18.0.1.1"
    jreMinVersion = "18"
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