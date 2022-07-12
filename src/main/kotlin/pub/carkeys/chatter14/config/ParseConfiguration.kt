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

import cc.ekblad.toml.tomlMapper
import pub.carkeys.chatter14.I18N
import pub.carkeys.chatter14.ffxiv.Universe
import pub.carkeys.chatter14.logger

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
data class ParseConfiguration(
    val dryRun: Boolean = false,
    val replaceIfExists: Boolean = false,
    val includeEmotes: Boolean = true,
    val performRename: Boolean = true,
    val dataCenterName: String = "Crystal",
    val server: String = "Zalera",
    val renames: Map<String, String> = mapOf(),
    val groupList: List<GroupEntry> = listOf(),
) {
    val groups: Map<String, Group> by lazy {
        groupList.associateBy { it.shortName }.plus(everyone.shortName to everyone)
    }

    val dataCenter: Universe.DataCenter by lazy { Universe[dataCenterName]!! }

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
        val theLabel: String?,
        val theShortName: String? = null,
        val participants: List<String>,
    ) : Group {
        override val label: String
            get() = theLabel!!

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
    data class GroupEveryone(override val label: String = I18N.labelEveryone.toString()) : Group {
        override fun matches(name: String): Boolean {
            return true
        }
    }

    /**
     * Validates the configuration for consistency. Throw an IllegalArgumentException if there
     * are any violations.
     */
    fun validate() {
        var hasError = false
        if (Universe[dataCenterName] == null) {
            logger.error(
                I18N.logInvalidDataCenterName.format(dataCenterName, Universe.dataCenterNames.joinToString(", "))
            )
            hasError = true
        } else {
            if (!dataCenter.servers.contains(server)) {
                logger.error(
                    I18N.logInvalidServerName.format(
                        server, dataCenterName, Universe[dataCenterName]?.servers?.sorted()?.joinToString(
                            ", "
                        ) ?: "???"
                    )
                )
                hasError = true
            }
        }
        val groupShortNames = mutableSetOf<String>()
        val groupLabels = mutableSetOf<String>()
        groupList.forEachIndexed { index, groupEntry ->
            hasError = hasError or validateGroupEntry(index, groupEntry, groupShortNames, groupLabels)
        }
        if (hasError) throw ChatterConfigurationException(I18N.logConfigurationErrors)
    }

    @Suppress("DuplicatedCode")
    private fun validateGroupEntry(
        index: Int,
        groupEntry: GroupEntry,
        groupShortNames: MutableSet<String>,
        groupLabels: MutableSet<String>,
    ): Boolean {
        var hasError = false
        if (groupEntry.theShortName == null || groupEntry.theShortName.isBlank()) {
            logger.error(I18N.logGroupMissingShortName.format(index))
            hasError = true
        }
        if (groupShortNames.contains(groupEntry.shortName)) {
            logger.error(I18N.logGroupDuplicateShortName.format(groupEntry.shortName))
            hasError = true
        }
        groupShortNames.add(groupEntry.shortName)
        if (groupEntry.theLabel == null || groupEntry.theLabel.isBlank()) {
            logger.error(I18N.logGroupMissingLabel.format(index))
            hasError = true
        }
        if (groupShortNames.contains(groupEntry.label)) {
            logger.error(I18N.logGroupDuplicateLabel.format(groupEntry.label))
            hasError = true
        }
        groupLabels.add(groupEntry.label)
        if (groupEntry.participants.isEmpty()) {
            logger.error(I18N.logGroupParticipantsMissing.format(groupEntry.shortName))
            hasError = true
        }
        groupEntry.participants.forEachIndexed { pIndex, s ->
            if (s.isBlank()) {
                logger.error(I18N.logGroupParticipantNameMissing.format(pIndex, groupEntry.shortName))
                hasError = true
            }
        }
        return hasError
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

    /**
     * Writes the current state of the configuration to the given Appendable.
     */
    fun write(appendable: Appendable) {
        configurationIO.write(appendable, this)
    }

    companion object {
        val logger by logger()

        /**
         * The everyone accepted group.
         */
        val everyone = GroupEveryone()

        private val configurationFiles = listOf(".chatter14.toml", "chatter14.toml")

        private val defaultValues = ParseConfiguration()

        /**
         * Used by the Toml parser to map configuration file field names to code names.
         */
        private val mapper = tomlMapper {
            mapping<ParseConfiguration>("group" to "groupList")
            mapping<ParseConfiguration>("datacenter" to "dataCenterName")
            mapping<GroupEntry>("label" to "theLabel")
            mapping<GroupEntry>("shortname" to "theShortName")
        }

        private val configurationIO = ConfigurationIO(filenames = configurationFiles, mapper = mapper)

        fun read(): ParseConfiguration {
            return configurationIO.read(defaultValues)
        }
    }
}