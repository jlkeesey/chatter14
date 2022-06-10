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
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

/**
 * The application class. The run() method will be invoked by the Clikt command line
 * processing library.
 *
 * @property config the ParseConfig to use to parse any files.
 */
class Application(private val config: ParseConfig) : CliktCommand(name = "logparse") {
    val version = loadVersion().removeSuffix("-SNAPSHOT")
//    val version = (System.getProperty("build.version") ?: "1.4").removeSuffix("-SNAPSHOT")

    init {
        versionOption(version)
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
    ).default(ParseConfig.everyone.label)

    private val files: List<File> by argument().file(mustExist = false, canBeFile = true).multiple()

    /**
     * Entry point for the main processing.
     */
    override fun run() {
        config.validate() // Do this after the command line parsing in case the parsing changed anything.
        val parseOptions = config.asOptions().copy(
            dryRun = dryRun,
            forceReplace = replace,
            includeEmotes = includeEmotes,
            group = config.groups[group]!!,
            files = files.toMutableList() // TODO get rid of toMutableList() if possible
        )
        logger.info("windowed = $windowed")
        logger.info("options = $parseOptions")
        if (windowed) {
            executeWindowed(parseOptions)
        } else {
            executeCommandLine(parseOptions)
        }
    }

    /**
     * Starts the application in windowed, drag-and-drop mode.
     */
    private fun executeWindowed(options: ParseOptions) {
        registerFonts()

        // We wait for the panel to close before returning so that we keep the log sequence correct.
        // Without the latch, the main thread would exit as soon as the panel was started.
        val panelClosedLatch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            val panel = DropPanel(parseConfig = config, parseOptions = options)
            panel.addWindowListener(object : WindowAdapter() {
                override fun windowClosed(e: WindowEvent?) {
                    super.windowClosed(e)
                    panelClosedLatch.countDown()
                }
            })
        }
        panelClosedLatch.await()
    }

    /**
     * Executes the LogParser from the command line.
     */
    private fun executeCommandLine(options: ParseOptions) {
        try {
            LogParse(options).process()

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
            logger.error(e)
            exitProcess(3)
        }
    }

    /**
     * Register needed fonts with the Swing environment. All fonts in the resources/fonts
     * directory will be registered. Current we only load TrueType files.
     */
    private fun registerFonts() {
        val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val trueTypeFile = Regex("^.*\\.ttf$")
        this.javaClass.getResourceAsStream("/fonts").use { directory ->
            BufferedReader(InputStreamReader(directory!!)).use { reader ->
                reader.lineSequence().filter { line ->
                    trueTypeFile.matches(line)
                }.forEach {
                    registerFont(graphicsEnvironment, "/fonts/$it")
                }
            }
        }
    }

    /**
     * Register a single font from a resource path.
     *
     * @param graphicsEnvironment the environment to load the font into.
     * @param filename the fully qualified path name in the resource form.
     * @param format the font format.
     */
    private fun registerFont(
        graphicsEnvironment: GraphicsEnvironment,
        filename: String,
        format: Int = Font.TRUETYPE_FONT,
    ) {
        logger.info("Registering font $filename")
        this.javaClass.getResourceAsStream(filename).use { input ->
            try {
                val customFont = Font.createFont(format, input)
                graphicsEnvironment.registerFont(customFont)
            } catch (e: IOException) {
                logger.error("IO error reading font '${filename}'", e)
            } catch (e: FontFormatException) {
                logger.error("Font format error reading font '${filename}'", e)
            }
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
            logger.traceEntry()
            try {
                val config = ParseConfig.read()
                Application(config).main(args)
            } catch (e: ShutdownException) {
                // This is present to prevent any automatic exception printing
            }
            logger.traceExit()
        }

        /**
         * Returns the version from the version.properties file which is generated by the build.
         */
        private fun loadVersion(): String {
            val properties = Properties()
            try {
                Thread.currentThread().contextClassLoader.getResourceAsStream("version.properties").use {
                    properties.load(it)
                }
            } catch (e: IOException) {
                logger.error("Failed to load version.properties", e)
            }
            return properties["logparse.version"] as? String ?: "1.4"
        }
    }
}