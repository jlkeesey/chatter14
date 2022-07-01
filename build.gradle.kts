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

import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.preRelease
import java.io.FileNotFoundException
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage") plugins {
    kotlin("jvm") version "1.7.0"
    application
    alias(libs.plugins.badassRuntime)
    alias(libs.plugins.dokka)
    alias(libs.plugins.axionRelease)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

project.version = scmVersion.version

val applicationName: String by extra(rootProject.name)
val applicationTitle: String by extra("Chatter XIV")
val applicationVersion: String = scmVersion.version

val resourceGenerationDir by extra("${buildDir}/generated_src/main/resources")
val resourceVersionProperties by extra("${resourceGenerationDir}/application.properties")
val applicationPackage = "pub.carkeys.$applicationName"
val applicationMainClassName = "$applicationPackage.MainKt"

group = applicationPackage

repositories {
    flatDir {
        dirs("../4koma/build/libs")
    }
}

dependencies {
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.kotlin.reflect)
    implementation(libs.clikt)
    implementation(libs.bundles.log4j)
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.0")

    // When 1.0.3 releases we can change back to the repository version as the fix we need will be in it
    // implementation(libs.four.koma) and we can also remove the antlr reference
    implementation(files("../4koma/build/libs/4koma-1.0.2.jar"))
    implementation("org.antlr", "antlr4-runtime", "4.10.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.logback)
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

    @Suppress("SuspiciousCollectionReassignment") withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }

    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))
    }

    register("loadAxionAutorization") {
        val authorization = loadProperties("release.properties")
        scmVersion.repository.customUsername = authorization["axion.username"] as String
        scmVersion.repository.customPassword = authorization["axion.password"] as String
        if (project.hasProperty("releaseDryRun")) {
            ext.set("release.dryRun", true)
        }
    }

    named("release") {
        dependsOn("loadAxionAutorization")
    }
}

application {
    mainClass.set(applicationMainClassName)
}

/**
 * Creates installers for this product. Currently this only creates a Windows MSI installer.
 * For Linux et al just user the tar file.
 */
runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(
        listOf(
            "java.compiler",
            "java.datatransfer",
            "java.desktop",
            "java.logging",
            "java.management",
            "java.naming",
            "java.prefs",
            "java.rmi",
            "java.scripting",
            "java.sql",
            "java.xml",
            "jdk.unsupported"
        )
    )
    jpackage {
        appVersion = scmVersion.version.removeSuffix("-SNAPSHOT")
        outputDir = "jpackage"
        imageName = applicationName
        imageOptions = listOf("--icon", "src/main/installer/$applicationName.ico")
        installerName = applicationName
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            installerType = "msi"
            installerOptions = listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut")
        }
    }
}

/**
 * Handles the versioning of the product. This creates several useful tasks. The most useful
 * are currentVersion which returns the current version as this plugin knows it and release
 * which makes the current code a new release by tagging the current commit and pushing
 * the changes to the origin. See https://axion-release-plugin.readthedocs.io/en/latest/
 */
scmVersion {
    useHighestVersion = true
    versionIncrementer("incrementMinor")
    hooks {
        preRelease {
            preFileUpdate(file = "README.md",
                          pattern = { previousVersion, _ -> "Stable version $previousVersion" },
                          replacement = { currentVersion, _ -> "Stable version $currentVersion" })
            preFileUpdate(file = "README.md",
                          pattern = { previousVersion, _ -> "Stable-$previousVersion-" },
                          replacement = { currentVersion, _ -> "Stable-$currentVersion-" })
        }
    }
}

/**
 * Helper method to make calling the fileUpdate action of the pre-release hoock of the Axion
 * release plugin. Currently it is written in Groovy and uses Groovy closures which are a
 * pain to use in Kotlin. They say they are working on changing to a more Kotlin friendly
 * version and when they do we can remove this.
 *
 * @param file the name of the file to process.
 * @param pattern a lambda returning the pattern to match.
 * @param replacement a lambda returning the replacement string.
 */
fun HooksConfig.preFileUpdate(
    file: String,
    pattern: (String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext) -> String,
    replacement: (String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext) -> String,
) {
    pre(
        "fileUpdate", mapOf(
            "file" to file, "pattern" to KotlinClosure2(pattern), "replacement" to KotlinClosure2(replacement)
        )
    )
}

/**
 * Loads the given properties file and returns the Properties created from it.
 */
fun loadProperties(filename: String): Properties {
    val file = rootProject.file(filename)
    if (!file.exists()) {
        throw FileNotFoundException("$filename file is missing")
    }
    return Properties().apply {
        load(file.reader())
    }
}
