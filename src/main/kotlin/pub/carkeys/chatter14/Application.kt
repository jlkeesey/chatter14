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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import org.apache.logging.log4j.Level
import pub.carkeys.chatter14.config.ChatterConfigurationException
import pub.carkeys.chatter14.config.ParseConfiguration
import pub.carkeys.chatter14.ffxiv.Universe
import pub.carkeys.chatter14.log4j.LoggerAppendable
import pub.carkeys.chatter14.processor.ActLogFileHandler
import pub.carkeys.chatter14.window.WindowManager
import java.io.File
import java.util.MissingResourceException

/**
 * The application class. The run() method will be invoked by the Clikt command line
 * processing library.
 *
 * @property config the ParseConfig to use to parse any files.
 * @property info the application info.
 * @property windowManager the window manager to use if windowed is requested.
 * @property fileHandler the file handler to use. This will be used by both the command
 *     line and windowed.
 */
class Application(
    private val config: ParseConfiguration,
    private val info: ApplicationInfo,
    private val windowManager: WindowManager = WindowManager(),
    private val fileHandler: ActLogFileHandler = ActLogFileHandler(),
) : CliktCommand(name = info.name) {
    init {
        versionOption(info.version)
    }

    override val commandHelp: String
        get() = I18N.commandHelpText.toString()

    override val commandHelpEpilog: String
        get() {
            val builder = StringBuilder()
            builder.append("```\n")
            builder.append("${I18N.configuredGroupHeader}\n")
            builder.append("\n")
            config.groups.values.sortedBy { it.shortName }.forEach { group ->
                builder.append("  ${group.label} (${group.shortName})\n")
                if (group is ParseConfiguration.GroupEntry) {
                    group.participants.sorted().forEach { participant ->
                        builder.append("    $participant\n")
                    }
                }
                builder.append("\n")
            }
            builder.append("```\n")
            return builder.toString()
        }

    private val logConfig by option("-c", "--config", help = I18N.commandLineLogConfigHelp.toString()).flag(
        default = false
    )
    private val logUniverse by option("-u", "--universe", help = I18N.commandLineLogUniverseHelp.toString()).flag(
        default = false
    )
    private val dryRun by option("-d", "--dryrun", help = I18N.commandLineDryRunHelp.toString()).flag(
        "-P", "--process", default = config.dryRun
    )
    private val replace by option("-r", "--replace", help = I18N.commandLineReplaceHelp.toString()).flag(
        "-S", "--no-replace", default = config.replaceIfExists
    )
    private val includeEmotes by option("-e", "--emotes", help = I18N.commandLineIncludeEmotesHelp.toString()).flag(
        "-E", "--no-emotes", default = config.includeEmotes
    )
    private val windowed by option("-w", "--window", help = I18N.commandLineWindowedHelp.toString()).flag(
        "-W", "--no-window", default = true
    )

    private val group by option("-g", "--group", help = I18N.commandLineGroupHelp.toString()).choice(
        config.groups.values.associate { Pair(it.shortName, it.shortName) }.toSortedMap(), ignoreCase = true
    ).default(ParseConfiguration.everyone.shortName)

    private val files: List<File> by argument(help = I18N.commandLineFilesToProcessHelp.toString())
        .file(mustExist = false, canBeFile = true)
        .multiple()

    /**
     * Entry point for the main processing called by Clikt.
     */
    override fun run() {
        config.validate() // Do this after the command line parsing in case the parsing changed anything.
        val options = config.asOptions().copy(
            dryRun = dryRun,
            forceReplace = replace,
            group = config.groups[group]!!,
            includeEmotes = includeEmotes,
            windowed = windowed
        )
        logConfiguration()
        logUniverse()
        if (options.windowed) {
            windowManager.start(options = options, config = config, info = info, fileHandler = fileHandler)
        } else {
            fileHandler.process(options = options, files = files)
        }
    }

    /**
     * Logs the current configuration if requested.
     */
    private fun logConfiguration() {
        if (logConfig) {
            logger.info(I18N.logConfigurationFileHeader.toString())
            LoggerAppendable(logger = logger, level = Level.INFO, indent = "   ").use {
                config.write(it)
            }
        }
    }

    /**
     * Logs the current universe definition if requested.
     */
    private fun logUniverse() {
        if (logUniverse) {
            logger.info(I18N.logUniverseDefinitionHeader.toString())
            LoggerAppendable(logger = logger, level = Level.INFO, indent = "   ").use {
                Universe.write(it)
            }
        }
    }

    companion object {
        private val logger by logger()

        /**
         * Main entry point for the application. We read the configuration file if present
         * then invoke the command line processing library to handle and command line
         * arguments which then invokes the run() method of our application class.
         *
         * This is only present because we need to load the configuration file before
         * starting the command line processing as the configuration can affect the
         * command line processing.
         */
        fun start(args: Array<String>) {
            try {
                logger.traceEntry()
                val info = ApplicationInfo.load()
                val config = ParseConfiguration.read()
                Application(config = config, info = info).main(args)
            } catch (e: MissingResourceException) {
                logger.error(e.localizedMessage, e)
            } catch (e: ChatterConfigurationException) {
                logger.error(e.localizedMessage)
            } catch (e: Exception) {
                logger.error(I18N.logUnexpectedError, e)
            } finally {
                logger.traceExit()
            }
        }
    }
}
