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

package pub.carkeys.chatter14.processor

import pub.carkeys.chatter14.I18N
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.ffxiv.ChatInfo
import pub.carkeys.chatter14.logger
import java.io.Reader
import java.io.Writer
import java.lang.Integer.max
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/**
 * The log processor. This parsers the given Reader as an ACT log, filters for the
 * requested items, and then writes them to the given Writer.
 */
class ActLogProcessor(
    private val parser: ActLogParser = ActLogParser(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * Process an Act log into an output containing the filtered chats.
     */
    fun process(name: String, options: ParseOptions, input: Reader, output: Writer) {
        val chatLog = parser.readLog(input, options)
        writeChats(name, output, chatLog)
    }

    /**
     * Writes the filtered contents to the target file. We fail if the target already
     * exists unless the force flag was supplied.
     */
    private fun writeChats(name: String, output: Writer, chatLog: List<ChatInfo>) {
        output.write("${I18N.outputHeader.format(OffsetDateTime.now(clock))}\n")
        if (chatLog.isEmpty()) {
            output.write("${I18N.outputNoLinesMatched}\n")
            logger.info("$name is empty")
            return
        }

        val nameMax = chatLog.map { it.shortName.length }.reduce { lhs, rhs -> max(lhs, rhs) }
        writeOut(output, chatLog, nameMax)
    }

    /**
     * Writes the chat log to the given Writer.
     */
    private fun writeOut(writer: Writer, chatLog: List<ChatInfo>, nameMax: Int) {
        val formatString = "%s %s %-${nameMax}.${nameMax}s: %s\n"
        chatLog.forEach { info ->
            val message = String.format(
                formatString, timestampFormatter.format(info.timestamp), info.typeName, info.shortName, info.msg
            )
            writer.write(message)
            logger.debug(message)
        }
    }

    companion object {
        private val logger by logger()

        private val timestampFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter()
    }
}