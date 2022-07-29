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

import java.lang.Integer.min
import java.text.MessageFormat
import java.util.*

/**
 * Defines the internationalization code specifically the message loading code.
 */
class I18N private constructor() {
    /**
     * Defines a translatable string.
     *
     * @property key the key used to lookup the message in the locale specific resource
     *     bundle.
     * @property fallback the fallback string when the key is not found in the bundle.
     * @property description the description of the string to help the translators
     *     translate the string. This will be written
     *     to the default message bundle as a comment.
     */
    class Message(private val key: String, private val fallback: String, private val description: String) {
        private val message: String by lazy {
            if (bundle.containsKey(key)) {
                bundle.getString(key)
            } else {
                fallback
            }
        }

        override fun toString(): String {
            // We need to invoke MessageFormat even if there are no replacement parameters
            // so that single quotes are handled the same. Having some lines need doubled
            // single quotes and some not would be too confusing.
            return format()
        }

        fun format(vararg values: Any): String {
            return MessageFormat.format(message, *values)
        }

        /**
         * Writes this message to the given Appendable. This is only used to create the
         * default message file so that it matches the code.
         */
        fun write(appendable: Appendable) {
            splitToFit(description).forEach { line ->
                appendable.append("# ").append(line).append('\n')
            }
            appendable.append(key).append("=").append(escapeNL(fallback))
        }
    }

    companion object {
        /**
         * The appropriate message bundle based on the current locale.
         *
         * NOTE: In the current system, this is only called once at startup time so
         * changing the locale after the application starts will have no effect on this
         * application.
         */
        private val bundle = ResourceBundle.getBundle(
            "pub.carkeys.chatter14.i18n.messages", Locale.getDefault(), Companion::class.java.classLoader
        )

        val defaultDataCenterNotFound = Message(
            "default_datacenter_not_found", "Default data center Crystal not defined.", """
                Displayed as an error message to indicate that the data center named
                Crystal is not defined in the loaded data center list.
            """.trimIndent()
        )

        val dataCenterDefinitionsNotLoaded = Message(
            "datacenter_definitions_not_loaded", "Data center definitions could not be loaded.", """
                Displayed as an error message to indicate that the builtin data center
                definition file could not be read.
            """.trimIndent()
        )

        val applicationPropertiesLoadFailed = Message(
            "application_properties_load_failed", "Failed to load application.properties.", """
                Displayed as a fatal error if the application.properties file can not be loaded
                at startup time.
            """.trimIndent()
        )

        val commandHelpText = Message(
            "command_help_text", """
            Extracts Final Fantasy 14 chat and emote lines from ACT logs.
            
            This application takes one or more ACT log files and extracts various chat and optionally 
            emote lines from the log. The lines can be further filtered by a list of user names.
            
            This command can be run in either command line or windowed modes. Command line mode
            is useful for experience users or inclusion in scripts. Windowed mode allows for 
            drag-and-drop actions to convert files. The default is to use windowed mode.
        """.trimIndent(), """
                Displayed as part of the command line help when the -h flag is present. This
                is the header that appears before the option help describing the overall 
                application.
            """.trimIndent()
        )

        val configuredGroupHeader = Message(
            "configured_group_header", "Configured groups:", """
                Displayed as a header over the list of configured groups as read from the
                configuration file.
            """.trimIndent()
        )

        val commandLineLogConfigHelp = Message(
            "command_line_log_config", "logs the config", """
                Help text for the log config (--config) command line option.
            """.trimIndent()
        )

        val commandLineLogUniverseHelp = Message(
            "command_line_log_universe", "logs the universe definition", """
                Help text for the log universe (--universe) command line option.
            """.trimIndent()
        )

        val commandLineDryRunHelp = Message(
            "command_line_dry_run", "process without creating output files", """
                Help text for the dry run (--dryrun) command line option.
            """.trimIndent()
        )

        val commandLineReplaceHelp = Message(
            "command_line_replace", "replace existing text files", """
                Help text for the replace files (--replace) command line option.
            """.trimIndent()
        )

        val commandLineIncludeEmotesHelp = Message(
            "command_line_include_emotes", "include emotes in the output", """
                Help text for the include emotes (--emotes) command line option.
            """.trimIndent()
        )

        val commandLineWindowedHelp = Message(
            "command_line_windowed", "display drag-and-drop target window", """
                Help text for the window (--window) command line option.
            """.trimIndent()
        )

        val commandLineGroupHelp = Message(
            "command_line_group", "group (list of users) to filter for", """
                Help text for the group (--group) command line option.
            """.trimIndent()
        )

        val commandLineFilesToProcessHelp = Message(
            "command_line_files", "The log files to process", """
                Help text for the list of files command line option.
            """.trimIndent()
        )

        val logConfigurationFileHeader = Message(
            "log_configuration_file_header", "Configuration file:", """
                Displayed as a header over the current configuration file values as parsed.
            """.trimIndent()
        )

        val logUniverseDefinitionHeader = Message(
            "log_universe_definition_header", "Universe definition:", """
                Displayed as a header over the current universe definition as parsed.
            """.trimIndent()
        )

        val logUnexpectedError = Message(
            "log_unexpected_error", "Something unexpected occurred.", """
                Displayed when an unexpected error occurs. Very general and hopefully never happens.
            """.trimIndent()
        )

        val logConfigurationReadError = Message(
            "log_configuration_read_error", "Error attempting to read one of the configuration: {0}", """
                Displayed when an error occurs trying to read a configuration file.
            """.trimIndent()
        )

        val logConfigurationFileExists = Message(
            "log_configuration_file_exists", "Reading configuration file {0}", """
                Displayed to log the name of the configuration file that is being read for this execution.
            """.trimIndent()
        )

        val logConfigurationUsingDefaults = Message(
            "log_configuration_using_defaults", "Reading default configuration resource {0}", """
                Displayed to log the name of the configuration resource that is being used
                to provide default values.
            """.trimIndent()
        )

        val labelEveryone = Message(
            "labelEveryone", "Everyone", """
                The label for the everyone group that represents all users.
            """.trimIndent()
        )

        val logInvalidDataCenterName = Message(
            "log_invalid_data_center_name", "'{0}' not a valid data center name. Valid names are: {1}", """
                Logged when a data center name is encountered that is not present in the list of 
                data centers that was loaded.
            """.trimIndent()
        )

        val logInvalidServerName = Message(
            "log_invalid_server_name",
            "'{0}' is not a valid server name in data center '{1}'. Valid names are: {2}",
            """
                Logged when a server name is encountered that is not present in the current 
                data center that was loaded.
            """.trimIndent()
        )

        val logConfigurationErrors = Message(
            "log_configuration_errors", "There were errors in the configuration.", """
                Logged when a configuration has been successfully loaded but the validation
                failed. Previous log message will have listed all of the problems.
            """.trimIndent()
        )

        val logGroupMissingShortName = Message(
            "log_group_missing_short_name", "Group definition at index {0,number,integer} is missing a shortName", """
                Logged when the configuration is validated and a group does not have a valid
                short name. It is either missing or blank.
            """.trimIndent()
        )

        val logGroupDuplicateShortName = Message(
            "log_group_duplicate_short_name",
            "There is more than one group definition with the same shortName: {0}",
            """
                Logged when the configuration is validated and a group's short name is the
                same as another group.
            """.trimIndent()
        )

        val logGroupMissingLabel = Message(
            "log_group_missing_label", "Group definition at index {0,number,integer} is missing a label", """
                Logged when the configuration is validated and a group does not have a valid
                label. It is either missing or blank.
            """.trimIndent()
        )

        val logGroupDuplicateLabel = Message(
            "log_group_duplicate_label", "There is more than one group definition with the same label: {0}", """
                Logged when the configuration is validated and a group's label is the
                same as another group.
            """.trimIndent()
        )

        val logGroupParticipantsMissing = Message(
            "log_group_participants_missing",
            "Group definition {0} is missing participants. Add at least one user to the participants list.",
            """
                Logged when the configuration is validated and a group has no participants listed.
                At least one participant is required.
            """.trimIndent()
        )

        val logGroupParticipantNameMissing = Message(
            "log_group_participant_name_missing",
            "The participant name at index {0,number,integer} of Group definition {1} is missing or blank.",
            """
                Logged when the configuration is validated and a group has a participant whose
                name is missing or blank.
            """.trimIndent()
        )

        val logProcessingFiles = Message(
            "log_processing_files", "Processing all log files in {0}", """
                Logged at the beginning of processing the list of log files.
            """.trimIndent()
        )

        val logInputFileMissing = Message(
            "log_input_file_missing", "{0}Input file {1} does not exist", """
                Logged when an input file name does not exist.
            """.trimIndent()
        )

        val logInputNameNotAFile = Message(
            "log_input_not_a_file", "{0}Input name {1} is not a file", """
                Logged when an input name exists but is not a file.
            """.trimIndent()
        )

        val logTargetExists = Message(
            "log_target_exists", "{0}Target file exists, skipping: {1}", """
                Logged when the target file name exists and the replace flag is false.
            """.trimIndent()
        )

        val logProcessingFile = Message(
            "log_processing_a_file", "{0}Processing {1}", """
                Logged when a log file is being processed.
            """.trimIndent()
        )

        val outputHeader = Message(
            "output_header", "# Created at {0}", """
                Written as the first line of each output file.
            """.trimIndent()
        )

        val outputNoLinesMatched = Message(
            "output_no_lines_matched", "# No lines matched the criteria", """
                Written after the header if there are no lines to write to the output.
            """.trimIndent()
        )

        val labelLog = Message(
            "label_log", "Log", """
                Label for button that opens the log window
            """.trimIndent()
        )

        val labelDryRun = Message(
            "label_dry_run", "Dry run", """
                Label for dry run checkbox
            """.trimIndent()
        )

        val labelEmotes = Message(
            "label_emotes", "Emotes", """
                Label for include emotes checkbox
            """.trimIndent()
        )

        val labelReplaceFiles = Message(
            "label_replace_files", "Replace files", """
                Label for replace files checkbox
            """.trimIndent()
        )

        val logRegisteringFont = Message(
            "label_registering_font", "Registering font {0}", """
                Logged when a font is being registered with the window manager.
            """.trimIndent()
        )

        val logFontReadError = Message(
            "label_font_read_error", "IO error reading font '{0}'", """
                Logged when a font cannot be read.
            """.trimIndent()
        )

        val logFontFormatError = Message(
            "label_font_format_error", "Font format error reading font '{0}'", """
                Logged when a font file is in the wrong format and cannot be processed.
            """.trimIndent()
        )

        /**
         * All of the translatable strings of the system. All new messages should be added
         * at the end of this list to simplify translation. DO NOT SORT this list.
         */
        private val messages = listOf(
            defaultDataCenterNotFound,
            dataCenterDefinitionsNotLoaded,
            applicationPropertiesLoadFailed,
            commandHelpText,
            configuredGroupHeader,
            commandLineLogConfigHelp,
            commandLineLogUniverseHelp,
            commandLineDryRunHelp,
            commandLineReplaceHelp,
            commandLineIncludeEmotesHelp,
            commandLineWindowedHelp,
            commandLineGroupHelp,
            commandLineFilesToProcessHelp,
            logConfigurationFileHeader,
            logUniverseDefinitionHeader,
            logUnexpectedError,
            logConfigurationReadError,
            logConfigurationFileExists,
            logConfigurationUsingDefaults,
            labelEveryone,
            logInvalidDataCenterName,
            logInvalidServerName,
            logConfigurationErrors,
            logGroupMissingShortName,
            logGroupDuplicateShortName,
            logGroupMissingLabel,
            logGroupDuplicateLabel,
            logGroupParticipantsMissing,
            logGroupParticipantNameMissing,
            logProcessingFiles,
            logInputFileMissing,
            logInputNameNotAFile,
            logTargetExists,
            logProcessingFile,
            outputHeader,
            outputNoLinesMatched,
            labelLog,
            labelDryRun,
            labelEmotes,
            labelReplaceFiles,
            logRegisteringFont,
            logFontReadError,
            logFontFormatError,
        )

        /**
         * Writes out all of the messages, in order, to the given Appendable. This is only
         * used to create the default message file so that it matches the code.
         */
        fun write(appendable: Appendable) {
            messages.forEach { message ->
                message.write(appendable)
                appendable.append("\n\n")
            }
        }

        private const val maxLineLength: Int = 78

        /**
         * Escape the newlines in the given string for writing to a properties file.
         */
        private fun escapeNL(input: String): String {
            return if (input.contains('\n')) {
                input.replace("\n", "\\n\\\n")
            } else {
                input
            }
        }

        /**
         * Splits the given string in parts that are no longer than the maxLineLength.
         */
        private fun splitToFit(input: String): List<String> {
            val result = mutableListOf<String>()
            val line = StringBuilder(input.replace("\n", " "))
            while (line.isNotEmpty()) {
                if (line.length <= maxLineLength) {
                    result.add(line.toString().trim())
                    break
                }
                var didOutput = false
                val end = min(line.length - 1, maxLineLength)
                for (i in end downTo 0) {
                    if (Character.isSpaceChar(line[i])) {
                        result.add(line.substring(0, i).trim())
                        var ind = i
                        while (ind < line.length && Character.isSpaceChar(line[ind])) {
                            ind++
                        }
                        line.delete(0, ind)
                        didOutput = true
                        break
                    }
                }
                if (!didOutput) {
                    result.add(line.substring(0, maxLineLength).trim())
                    line.delete(0, maxLineLength)
                }
            }
            return result
        }
    }
}

fun main() {
    I18N.write(System.out)
}