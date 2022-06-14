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

    private val group by option("-g", "--group", help = "group to filter for").choice(
        config.groups.values.associate { Pair(it.shortName, it.label) }, ignoreCase = true
    ).default(ParseConfiguration.everyone.label)

    private val files: List<File> by argument().file(mustExist = false, canBeFile = true).multiple()

    /**
     * Entry point for the main processing.
     */
    override fun run() {
        config.validate() // Do this after the command line parsing in case the parsing changed anything.
        val options = config.asOptions().copy(
            dryRun = dryRun,
            forceReplace = replace,
            includeEmotes = includeEmotes,
            group = config.groups[group]!!,
            windowed = windowed
        )
        if (options.windowed) {
            windowManager.start(config = config, options = options, info = info, fileHandler = fileHandler)
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
//
//            println("usage: chatter14 [ -a | -s ] [ -e ] file ...")
//            println()
//            println("  -a    capture all participants")
//            println("  -e    capture emotes from participants")
//            println("  -f    overwrites existing files")
//            println("  -s    capture people")
//            println("  file  one or more files to process, can include wildcard")
//            println()
//            println("If Log Parse is started with not command line options and files then it will")
//            println("start in windowed, drag-and-drop mode. This will display a window where files")
//            println("can be dragged to to be process.")
//            println()
//            println("Each file will be processed and the filtered results written to a new")
//            println("file of the same name with the extension changed to .txt")
//            println()
//            println("By default only chats from people (both last names) are written")
//            println("out.")
//            exitProcess(1)
