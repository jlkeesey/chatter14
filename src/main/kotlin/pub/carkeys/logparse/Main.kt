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

import kotlin.system.exitProcess

/**
 * Main entry point for the application.
 */
fun main(args: Array<String>) {
    // This is just for debugging purposes
    val showStackTrace = args.isNotEmpty() && args[0] == "--stacktrace"
    val arguments = if (showStackTrace) args.drop(1) else args.toList()
    try {
        LogParse(arguments).process()
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
