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

import java.awt.Font
import java.awt.FontFormatException
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException
import kotlin.io.path.forEachDirectoryEntry
import kotlin.system.exitProcess

/**
 * Main entry point for the application.
 */
fun main(args: Array<String>) {
    val logger = Logger()
    if (args.isEmpty()) {
        executeWindowed(logger)
    } else {
        executeCommandLine(logger, args)
    }
}

/**
 * Starts the application in windowed, drag-and-drop mode.
 */
private fun executeWindowed(logger: Logger) {
    registerFonts()
    DropPanel(logger)
}

/**
 * Executes the LogParser from the command line.
 */
private fun executeCommandLine(logger: Logger, args: Array<String>) {
    // This is just for debugging purposes
    val showStackTrace = args[0] == "--stacktrace"
    val arguments = if (showStackTrace) args.drop(1) else args.toList()
    try {
        val options = ParseOptions.parseArgs(arguments)
        LogParse(options).process(logger)
    } catch (e: UsageException) {
        println(e.localizedMessage)
        println()
        println("usage: logparse [ -a | -s ] [ -e ] file ...")
        println()
        println("  -a    capture all participants")
        println("  -e    capture emotes from participants")
        println("  -f    overwrites existing files")
        println("  -s    capture Aelym, Tifaa, and Fiora")
        println("  file  one or more files to process, can include wildcard")
        println()
        println("If LogParse is started with not command line options and files then it will")
        println("start in windowed, drag-and-drop mode. This will display a window where files")
        println("can be dragged to to be process.")
        println()
        println("Each file will be processed and the filtered results written to a new")
        println("file of the same name with the extension changed to .txt")
        println()
        println("By default only chats from Aelym and Tifaa (both last names) are written")
        println("out.")
        exitProcess(1)
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
