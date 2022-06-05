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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.awt.Font
import java.awt.FontFormatException
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException
import kotlin.io.path.forEachDirectoryEntry
import kotlin.system.exitProcess

class LogParseApp(private val config: ParseConfig) : CliktCommand(name = "logparse") {
    init {
        versionOption("1.3")
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
    private val showStackTrace by option("--stacktrace", help = "show stacktrace").flag(
        "--no-stacktrace", default = false
    )

    private val group by option("-g", "--group", help = "group to filter for").choice(
        config.groups.values.associate { Pair(it.shortName, it.label) }, ignoreCase = true
    ).default(ParseConfig.everyone.label)

    private val files: List<File> by argument().file(mustExist = false, canBeFile = true).multiple()

    override fun run() {
        config.validate()
        val parseOptions = config.asOptions().copy(
            dryRun = dryRun,
            forceReplace = replace,
            includeEmotes = includeEmotes,
            group = config.groups[group]!!,
            files = files.toMutableList() // TODO get rid of toMutableList() if possible
        )
        echo("options = $parseOptions")
        val logger = Logger()
        if (windowed) {
            executeWindowed(logger, parseOptions)
        } else {
            executeCommandLine(logger, parseOptions)
        }
    }

    /**
     * Starts the application in windowed, drag-and-drop mode.
     */
    private fun executeWindowed(logger: Logger, options: ParseOptions) {
        registerFonts()
        DropPanel(logger = logger, parseConfig = config, parseOptions = options)
    }

    /**
     * Executes the LogParser from the command line.
     */
    private fun executeCommandLine(logger: Logger, options: ParseOptions) {
        try {
            LogParse(options).process(logger)

//
//            println("usage: logparse [ -a | -s ] [ -e ] file ...")
//            println()
//            println("  -a    capture all participants")
//            println("  -e    capture emotes from participants")
//            println("  -f    overwrites existing files")
//            println("  -s    capture people")
//            println("  file  one or more files to process, can include wildcard")
//            println()
//            println("If LogParse is started with not command line options and files then it will")
//            println("start in windowed, drag-and-drop mode. This will display a window where files")
//            println("can be dragged to to be process.")
//            println()
//            println("Each file will be processed and the filtered results written to a new")
//            println("file of the same name with the extension changed to .txt")
//            println()
//            println("By default only chats from people (both last names) are written")
//            println("out.")
//            exitProcess(1)
        } catch (e: Exception) {
            System.err.println("Error: ${e.localizedMessage}")
            if (showStackTrace) {
                System.err.println()
                System.err.println(e.stackTraceToString())
            }
            exitProcess(3)
        }
    }

    /**
     * Register needed fonts with the Swing environment. All fonts in the src/main/fonts directory will be registered.
     */
    private fun registerFonts() {
        val directory = File("src/main/fonts")
        if (!directory.exists()) return // Nothing to do

        val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        directory.toPath().forEachDirectoryEntry("*.ttf") { p ->
            val file = p.toFile()
            if (file.isFile) {
                try {
                    val customFont = Font.createFont(Font.TRUETYPE_FONT, file)
                    graphicsEnvironment.registerFont(customFont)
                } catch (e: IOException) {
                    System.err.println("Cannot read font '${file.name}': ${e.localizedMessage}")
                } catch (e: FontFormatException) {
                    System.err.println("Cannot parse font '${file.name}': ${e.localizedMessage}")
                }
            }
        }
    }
}

/**
 * Main entry point for the application.
 */
fun main(args: Array<String>) {
    try {
        val config = ParseConfig.read()
        LogParseApp(config).main(args)
    } catch (e: ShutdownException) {
        // This is present to prevent any automatic exception printing
    }
}
