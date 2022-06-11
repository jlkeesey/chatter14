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

package pub.carkeys.chatter14

import cc.ekblad.toml.model.TomlException
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.tomlMapper
import java.io.BufferedReader
import java.io.File
import java.io.IOException

/**
 * The application settings from the configuration file(s).
 *
 * @property dryRun true if no output file should be generated.
 * @property replaceIfExists true if an existing output file should be replaced, otherwise it
 *     will be skipped.
 * @property includeEmotes true if emote "chat" lines should be included in the output.
 * @property dataCenterName the data center name this chat is from. Used to process server
 *     names.
 * @property serverName the name of the server the main user is from. Generally this will be
 *     the user that is executing this application.
 *     This server will not be included in the output.
 * @property performRename true if usernames should be renamed according to the renames list.
 * @property renames any username renames, usually used to shorten the names of common users.
 * @property groupEntries the group definitions. One of these will be used to process the
 *     files.
 */
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
    /**
     * The Groups that have been generated from the GroupEntry definitions.
     *
     * NOTE: this must not be private because the config parser can't handle private fields.
     */
    val groups: Map<String, Group> =
        groupEntries.associateBy({ it.label }, { it }).plus(Pair(everyone.label, everyone)).toSortedMap()

    /**
     * Returns the DataCenter from the configuration data center name.
     *
     * NOTE: this is a function because the config parser can't handle private fields.
     */
    private fun dataCenter(): DataCenter {
        return DataCenter.centers[dataCenterName]!!
    }

    /**
     * Defines a basic group definition.
     */
    interface Group {
        /**
         * Returns true if the given [name] matches the criteria of this Group.
         */
        fun matches(name: String): Boolean
        val label: String
        val shortName: String
            get() = label.lowercase()
    }

    /**
     * A group entry definition from the configuration file.
     *
     * @property label the label shown to the user.
     * @property theShortName the short name used in command line arguments.
     * @property participants the list of users to include.
     */
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

    /**
     * Group definition that includes everyone.
     */
    data class GroupEveryone(override val label: String = "Everyone") : Group {
        override fun matches(name: String): Boolean {
            return true
        }
    }

    /**
     * Returns this configuration as a ParseOptions object.
     */
    fun asOptions(): ParseOptions {
        return ParseOptions(
            dryRun = dryRun,
            forceReplace = replaceIfExists,
            includeEmotes = includeEmotes,
            dataCenter = dataCenter(),
            renames = if (performRename) renames.toMap() else mapOf()
        )
    }

    /**
     * Validates the configuration for consistency. Throw an IllegalArgumentException if there
     * are any violations.
     */
    fun validate() {
        val dataCenter = DataCenter.centers[dataCenterName]
                         ?: throw IllegalArgumentException("Unknown data center '$dataCenterName'")
        if (!dataCenter.servers.contains(serverName)) throw IllegalArgumentException("Unknown server '$serverName'")
    }

    companion object {
        private val logger by logger()

        /**
         * Used by the Toml parser to map configuration file field names to code names.
         */
        private val mapper = tomlMapper {
            mapping<ParseConfig>("group" to "groupEntries")
            mapping<ParseConfig>("datacenter" to "dataCenterName")
            mapping<ParseConfig>("server" to "serverName")
            mapping<GroupEntry>("shortname" to "theShortName")
        }

        /**
         * The everyone accepted group.
         */
        val everyone = GroupEveryone()

        /**
         * Reads the configuration from a file.
         *
         * @param filename the file to read defaults to <code>.chatter14.toml</code>
         */
        fun read(filename: String = ".chatter14.toml"): ParseConfig {
            val input = readConfigFile(filename)
            return if (input == null) ParseConfig() else parse(input)
        }

        /**
         * Parses a string into a ParseConfiguration. Normally this string comes from an external
         * file.
         */
        private fun parse(input: String): ParseConfig {
            return try {
                mapper.decodeWithDefaults(ParseConfig(), TomlValue.from(input))
            } catch (e: TomlException.DecodingError) {
                logger.error(cleanUpDecodingExceptionMessage(e))
                throw ShutdownException()
            }
        }

        /**
         * Returns a more user-friendly error message from the ones given by the Toml parser.
         */
        private fun cleanUpDecodingExceptionMessage(e: TomlException.DecodingError): String {
            return when (e.reason) {
                "no value found for required parameter 'label'" -> "A group label is missing"
                else                                            -> e.reason ?: e.localizedMessage
            }
        }

        /**
         * Reads the given configuration file and returns the contents as a string. Null is returned
         * if the file cannot be read.
         */
        private fun readConfigFile(filename: String): String? {
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