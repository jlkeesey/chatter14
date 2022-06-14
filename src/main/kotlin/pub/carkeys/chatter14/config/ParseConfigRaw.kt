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

package pub.carkeys.chatter14.config

import cc.ekblad.toml.model.TomlException
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.tomlMapper
import pub.carkeys.chatter14.logger
import java.io.BufferedReader
import java.io.File
import java.io.IOException

/**
 * The application settings from the configuration file(s). The Toml reader does not like
 * fields that it cannot write into and we need to massage the data before using it wo we
 * use a two step process: first we use the reader to read the raw data into these objects
 * and then we massage the data as necessary and create the objects that are used by this
 * application: ParseConfiguration and friends.
 *
 * @property dryRun true if no output file should be generated.
 * @property replaceIfExists true if an existing output file should be replaced, otherwise it
 *     will be skipped.
 * @property includeEmotes true if emote "chat" lines should be included in the output.
 * @property dataCenter the data center name this chat is from. Used to process server names.
 * @property server the name of the server the main user is from. Generally this will be the
 *     user that is executing this application. This server will not be included in the output.
 * @property performRename true if usernames should be renamed according to the renames list.
 * @property renames any username renames, usually used to shorten the names of common users.
 * @property groups the group definitions. One of these will be used to process the files.
 */
data class ParseConfigRaw(
    val dryRun: Boolean = false,
    val replaceIfExists: Boolean = false,
    val includeEmotes: Boolean = true,
    val performRename: Boolean = true,
    val dataCenter: String = "Crystal",
    val server: String = "Zalera",
    val renames: Map<String, String> = mapOf(),
    val groups: List<GroupRaw> = listOf(),
) {

    /**
     * A group entry definition from the configuration file.
     *
     * @property label the label shown to the user.
     * @property shortName the short name used in command line arguments.
     * @property participants the list of users to include.
     */
    data class GroupRaw(
        val label: String,
        val shortName: String? = null,
        val participants: List<String>,
    )

    companion object {
        private val logger by logger()

        /**
         * Used by the Toml parser to map configuration file field names to code names.
         */
        private val mapper = tomlMapper {
            mapping<ParseConfigRaw>("group" to "groups")
            mapping<ParseConfigRaw>("datacenter" to "dataCenter")
        }

        /**
         * Reads the configuration from a file.
         *
         * @param filename the file to read defaults to <code>.chatter14.toml</code>
         */
        fun read(filename: String = ".chatter14.toml"): ParseConfigRaw? {
            val input = readConfigFile(filename)
            return if (input == null) null else parse(input)
        }

        /**
         * Parses a string into a ParseConfiguration. Normally this string comes from an external
         * file.
         */
        private fun parse(input: String): ParseConfigRaw? {
            return try {
                mapper.decodeWithDefaults(ParseConfigRaw(), TomlValue.from(input))
            } catch (e: TomlException.DecodingError) {
                logger.error(cleanUpDecodingExceptionMessage(e), e)
                null
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
                logger.error("Error attempting to read the configuration file $filename", e)
                return null
            }
        }
    }
}