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

package pub.carkeys.logparse

import cc.ekblad.toml.model.TomlException
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.tomlMapper
import java.io.BufferedReader
import java.io.File
import java.io.IOException

data class ParseConfig(
    val dryRun: Boolean = false,
    val replaceIfExists: Boolean = false,
    val includeEmotes: Boolean = true,
    val performRename: Boolean = true,
    val dataCenterName: String = "Crystal",
    val serverName: String = "Zalera",
    val renames: Map<String, String> = mapOf(),
    val groupEntries: List<GroupEntry> = listOf(),
) {
    val groups: Map<String, Group> =
        groupEntries.associateBy({ it.label }, { it }).plus(Pair(everyone.label, everyone)).toSortedMap()

    private fun dataCenter(): DataCenter {
        return DataCenter.centers[dataCenterName]!!
    }

    interface Group {
        fun matches(name: String): Boolean
        val label: String
        val shortName: String
            get() = label.lowercase()
    }

    data class GroupEntry(
        override val label: String,
        val theShortName: String? = null,
        val participants: List<String>,
    ) : Group {
        override fun matches(name: String): Boolean {
            return participants.contains(name)
        }

        override val shortName: String
            get() {
                return theShortName ?: labelToShortName()
            }

        private fun labelToShortName(): String {
            return label.filter { it.isLetterOrDigit() || it == ' ' }.replace(' ', '-').lowercase()
        }
    }

    data class GroupEveryone(override val label: String = "Everyone") : Group {
        override fun matches(name: String): Boolean {
            return true
        }
    }

    fun asOptions(): ParseOptions {
        return ParseOptions(
            dryRun = dryRun,
            forceReplace = replaceIfExists,
            includeEmotes = includeEmotes,
            dataCenter = dataCenter(),
            renames = if (performRename) renames.toMap() else mapOf()
        )
    }

    fun validate() {
        val dataCenter = DataCenter.centers[dataCenterName]
                         ?: throw IllegalArgumentException("Unknown data center '$dataCenterName'")
        if (!dataCenter.servers.contains(serverName)) throw IllegalArgumentException("Unknown server '$serverName'")
    }

    companion object {
        private val mapper = tomlMapper {
            mapping<ParseConfig>("group" to "groupEntries")
            mapping<ParseConfig>("datacenter" to "dataCenterName")
            mapping<ParseConfig>("server" to "serverName")
            mapping<GroupEntry>("shortname" to "theShortName")
        }

        val everyone = GroupEveryone()

        fun read(filename: String = ".logparse.toml"): ParseConfig {
            val input = readConfigFile(filename)
            return if (input == null) ParseConfig() else parse(input)
        }

        private fun parse(input: String): ParseConfig {
            return try {
                mapper.decodeWithDefaults(ParseConfig(), TomlValue.from(input))
            } catch (e: TomlException.DecodingError) {
                System.err.println(cleanUpDecodingExceptionMessage(e))
                throw ShutdownException()
            }
        }

        private fun cleanUpDecodingExceptionMessage(e: TomlException.DecodingError): String {
            return when (e.reason) {
                "no value found for non-nullable parameter 'label'" -> "A group label is missing"
                else                                                -> e.reason ?: e.localizedMessage
            }
        }

        private fun readConfigFile(filename: String): String? {
//            return """
//                dryRun = true
//                forceReplace = true
//                includeEmotes = true
//
//                [[group]]
//                label = "My, Myself, and Irene"
//                participants = [
//                   "R.L Stein",
//                   "Det. Poirot",
//                ]
//            """.trimIndent()

            try {
                var file = File(filename)
                if (!file.exists()) {
                    val home = System.getProperty("user.home", ".")
                    val homeDir = File(home)
                    if (homeDir.exists()) {
                        file = File(homeDir, filename)
                    }
                }
                if (!file.exists()) return null
                val bufferedReader: BufferedReader = file.bufferedReader()
                return bufferedReader.use { it.readText() }
            } catch (e: IOException) {
                // TODO log this somewhere
                return null
            }
        }
    }
}