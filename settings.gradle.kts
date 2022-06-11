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

rootProject.name = "logparse"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinx-coroutines", "1.6.1")
            version("kotlin-reflect", "1.6.21")
            version("four-koma", "1.0.1")
            version("clikt", "3.4.2")
            version("log4j", "2.17.2")
            version("junit5", "5.8.2")
            version("kotest", "5.3.0")
            version("mockk", "1.12.4")
            version("mockk-junit5", "2.0.0")

            library(
                "kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core"
            ).versionRef("kotlinx-coroutines")
            library(
                "kotlinx-coroutines-swing", "org.jetbrains.kotlinx", "kotlinx-coroutines-swing"
            ).versionRef("kotlinx-coroutines")
            library(
                "kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect"
            ).versionRef("kotlin-reflect")
            library(
                "four-koma", "cc.ekblad", "4koma"
            ).versionRef("four-koma")
            library(
                "clikt", "com.github.ajalt.clikt", "clikt"
            ).versionRef("clikt")
            library(
                "log4j-api", "org.apache.logging.log4j", "log4j-api"
            ).versionRef("log4j")
            library(
                "log4j-core", "org.apache.logging.log4j", "log4j-core"
            ).versionRef("log4j")
            library(
                "junit5", "org.junit.jupiter", "junit-jupiter"
            ).versionRef("junit5")
            library(
                "kotest-assertions-core", "io.kotest", "kotest-assertions-core"
            ).versionRef("kotest")
            library(
                "kotest-property", "io.kotest", "kotest-property"
            ).versionRef("kotest")
            library(
                "mockk", "io.mockk", "mockk"
            ).versionRef("mockk")
            library(
                "mockk-junit5", "com.github.erikhuizinga", "mockk-junit5"
            ).versionRef("mockk-junit5")

            bundle("kotlinx-coroutines", listOf("kotlinx-coroutines-core", "kotlinx-coroutines-swing"))
            bundle("log4j", listOf("log4j-api", "log4j-core"))
            bundle("kotest", listOf("kotest-assertions-core", "kotest-property"))
            bundle("kotestz", listOf("kotest-assertions-core", "kotest-property"))
        }
    }
}
