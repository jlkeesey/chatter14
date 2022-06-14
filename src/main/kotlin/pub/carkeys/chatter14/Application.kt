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
import pub.carkeys.chatter14.config.ParseConfiguration
import pub.carkeys.chatter14.processor.ActLogFileHandler
import pub.carkeys.chatter14.window.WindowManager
import java.io.File

/**
 * The application class. The run() method will be invoked by the Clikt command line
 * processing library.
 *
 * @property config the ParseConfig to use to parse any files.
 * @property info the application info.
 * @property windowManager the window manager to use if windowed is requested.
 * @property fileHandler the file handler to use. This will be used by both the command line
 *     and windowed.
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
        get() = """
            Extracts chat and emote lines from ACT logs.
            
            This application takes one or more ACT log files and extracts various char and optionally 
            emote lones from the log. The lines can be further filtered by a list of user names.
            
            This command can be run in either command line or windowed modes. Command line mode
            is useful for experience users or inclusion in scripts. Windowed mode allow for 
            drag-and-drop actions to convert files. The default is to use windowed mode but that
            can be changed with the -W option.
        """.trimIndent()

    private val dryRun by option("-d", "--dryrun", help = "process without creating output files").flag(
        "-P", "--process", default = config.dryRun
    )
    private val replace by option("-r", "--replace", help = "replace existing text files").flag(
        "-S", "--no-replace", default = config.replaceIfExists
    )
    private val includeEmotes by option("-e", "--emotes", help = "include emotes in the output").flag(
        "-E", "--no-emotes", default = config.includeEmotes
    )
    private val windowed by option("-w", "--window", help = "display drag-and-drop target window").flag(
        "-W", "--no-window", default = true
    )

    private val group by option("-g", "--group", help = "group (list of users) to filter for").choice(
        config.groups.values.associate { Pair(it.shortName, it.shortName) }.toSortedMap(), ignoreCase = true
    ).default(ParseConfiguration.everyone.shortName)

    private val files: List<File> by argument(help = "The log files to process")
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
        if (options.windowed) {
            windowManager.start(options = options, config = config, info = info, fileHandler = fileHandler)
        } else {
            fileHandler.process(options = options, files = files)
        }
    }

    companion object {
        private val logger by logger()

        /**
         * Main entry point for the application. We read the configuration file if present then
         * invoke the Clikt command line processing library to handle and command line arguments
         * which then invokes the run() method of our application class.
         *
         * This is only present because we need to load the configuration file before starting the
         * command line processing as the configuration can affect the command line processing.
         */
        fun start(args: Array<String>) {
            try {
                logger.traceEntry()
                val info = ApplicationInfo.load()
                val config = ParseConfiguration.read()
                if (config != null) {
                    Application(config = config, info = info).main(args)
                }
            } catch (e: Exception) {
                logger.error("Something unexpected occured.", e)
            } finally {
                logger.traceExit()
            }
        }
    }
}
