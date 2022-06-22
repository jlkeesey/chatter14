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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage") plugins {
    kotlin("jvm") version libs.versions.kotlin.asProvider()
    application
    alias(libs.plugins.release)
    alias(libs.plugins.launch4j)
    alias(libs.plugins.dokka)
    alias(libs.plugins.gradleVersions)
}

val applicationName: String by extra(rootProject.name)
val applicationTitle: String by extra("Chatter XIV")
val applicationVersion: String by extra(project.version.toString())

val resourceGenerationDir by extra("${buildDir}/generated_src/main/resources")
val resourceVersionProperties by extra("${resourceGenerationDir}/application.properties")
val applicationPackage = "pub.carkeys.$applicationName"
val applicationMainClassName = "$applicationPackage.MainKt"

group = applicationPackage

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    //maven(url = "https://kotlin.bintray.com/kotlinx")
    flatDir {
        dirs("../4koma/build/libs")
    }
}

dependencies {
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.kotlin.reflect)
    //implementation(libs.four.koma)
    implementation(files("../4koma/build/libs/4koma-1.0.2.jar"))
    implementation("org.antlr:antlr4:4.10.1")
    implementation(libs.clikt)
    implementation(libs.bundles.log4j)
//    implementation("com.akuleshov7:ktoml-core:0.2.11")
    implementation("ch.qos.logback:logback-classic:1.2.10")

    testImplementation(kotlin("test"))
    testImplementation(libs.junit5)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk.core)
    testImplementation(libs.mockk.junit5)
}

tasks {
    val versionProperties by registering(WriteProperties::class) {
        description = "Generated application info property file"
        comment = " Generated application info property file"
        outputFile = file(resourceVersionProperties)
        encoding = "UTF-8"
        property("application.name", applicationName)
        property("application.title", applicationTitle)
        property("application.version", applicationVersion)
    }

    processResources {
        from(versionProperties)
    }

    test {
        useJUnitPlatform()

        systemProperty("java.util.logging.config.file", "${project.buildDir}/resources/test/logging-test.properties")

        testLogging {
            showStandardStreams = true
        }
    }

    withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
        outfile = "${rootProject.name}.exe"
        mainClassName = applicationMainClassName
        icon = "$projectDir/icons/Iconka-Cat-Shadows-Cat-shadow.ico"
        jvmOptions = setOf("-Dbuild.version=${version}")
        productName = applicationTitle
        bundledJrePath = "C:/Program Files/Java/jdk-18.0.1.1"
        jreMinVersion = "1.8.0"
    }

    @Suppress("SuspiciousCollectionReassignment") withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }

    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))
    }
}

application {
    mainClass.set(applicationMainClassName)
}