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

/**
 * The application settings from the configuration file(s).
 *
 * @property dryRun true if no output file should be generated.
 * @property replaceIfExists true if an existing output file should be replaced, otherwise it
 *     will be skipped.
 * @property includeEmotes true if emote "chat" lines should be included in the output.
 * @property dataCenter the data center this chat is from. Used to process server names.
 * @property server the name of the server the main user is from. Generally this will be the
 *     user that is executing this application. This server will not be included in the output.
 * @property performRename true if usernames should be renamed according to the renames list.
 * @property renames any username renames, usually used to shorten the names of common users.
 * @property groups the group definitions. One of these will be used to process the files.
 */
class ParseConfiguration(raw: ParseConfigRaw) {
    private val performRename: Boolean = raw.performRename
    private val dataCenter: DataCenter = DataCenter.centers[raw.dataCenter]!!
    private val server: String = raw.server
    private val renames: Map<String, String> = raw.renames

    val dryRun: Boolean = raw.dryRun
    val replaceIfExists: Boolean = raw.replaceIfExists
    val includeEmotes: Boolean = raw.includeEmotes
    val groups: Map<String, Group> = mapGroupEntries(raw.groups)

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
     * Validates the configuration for consistency. Throw an IllegalArgumentException if there
     * are any violations.
     */
    fun validate() {
        if (!dataCenter.servers.contains(server)) throw IllegalArgumentException("Unknown server '$server'")
    }

    /**
     * Returns this configuration as a ParseOptions object.
     */
    fun asOptions(): ParseOptions {
        return ParseOptions(
            dryRun = dryRun,
            forceReplace = replaceIfExists,
            includeEmotes = includeEmotes,
            dataCenter = dataCenter,
            renames = if (performRename) renames.toMap() else mapOf()
        )
    }

    companion object {
        /**
         * The everyone accepted group.
         */
        val everyone = GroupEveryone()

        fun read(): ParseConfiguration {
            val rawConfig = ParseConfigRaw.read()
            return ParseConfiguration(rawConfig)
        }

        private fun mapGroupEntries(rawGroups: List<ParseConfigRaw.GroupRaw>): Map<String, Group> {
            return rawGroups.map {
                GroupEntry(
                    label = it.label,
                    theShortName = it.shortName ?: it.label.lowercase(),
                    participants = it.participants,
                )
            }.associateBy({ it.label }, { it }).plus(Pair(everyone.label, everyone)).toSortedMap()
        }
    }
}