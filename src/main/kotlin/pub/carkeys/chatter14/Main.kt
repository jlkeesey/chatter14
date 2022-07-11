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

import org.apache.logging.log4j.Level
import pub.carkeys.chatter14.log4j.loggerLevel

private const val ARG_LOGGING_PREFIX = "--logging="

/**
 * Main entry point for the application.
 */
fun main(args: Array<String>) {
    val remainingArgs = handleLoggingLevelArgument(args)
    Application.start(remainingArgs)
}

/**
 * Reads the arguments for a logging level setting value and if present uses that to set the
 * current logging level of the system. If the parameter is present it is removed from the
 * command line before passing it on to the Application. This needs to be handled here
 * before any other processing so that the logging level can be changed for all logging
 * messages.
 *
 * We process all setting arguments int order, so the last one will win.
 *
 * @param args the command line arguments.
 * @return the command line arguments with any logging level setting values removed.
 */
private fun handleLoggingLevelArgument(args: Array<String>): Array<String> {
    args.filter { it.startsWith(ARG_LOGGING_PREFIX) }.forEach {
        val name = it.removePrefix(ARG_LOGGING_PREFIX)
        val level = Level.toLevel(name, Level.WARN)
        loggerLevel(level)
    }
    return args.filter { !it.startsWith(ARG_LOGGING_PREFIX) }.toTypedArray()
}
