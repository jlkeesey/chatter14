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

package pub.carkeys.chatter14.ffxiv

import pub.carkeys.chatter14.config.ParseOptions
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Contains the parsed information from a log line. This only captures chat and emote types
 * which the logs basically treat as the same.
 */
data class ChatInfo(
    val lineNumber: Int,
    val name: String,
    val shortName: String,
    val code: ChatCode,
    val msg: String,
    val timestamp: OffsetDateTime,
) {
    val typeName: String
        get() = code.type.shortName

    companion object {
        /**
         * This should be used to construct new ChatInfo objects as it massages the data into a clean
         * form.
         */
        fun create(
            options: ParseOptions, lineNumber: Int, name: String, code: String, msg: String, timestamp: String,
        ): ChatInfo {
            val cleanName = cleanUpName(options, name)
            return ChatInfo(
                lineNumber = lineNumber,
                name = cleanName,
                shortName = options.renames[cleanName] ?: cleanName,
                code = parseCode(code, cleanName),
                msg = cleanUpMsg(options, msg, cleanName),
                timestamp = parseTimestamp(timestamp)
            )
        }

        private fun parseTimestamp(timestamp: String): OffsetDateTime {
            return OffsetDateTime.parse(timestamp, timestampParser)
        }

        private fun parseCode(code: String, name: String): ChatCode {
            if (name.isEmpty()) {
                return ChatCode.OTHER
            }
            return ChatCode.fromCode(code)
        }

        /**
         * Cleans up the message. We remove any mention of a world as that is just messy. We only
         * remove the world if it is abutted to the previous word and that signifies that it is part
         * of a name. We only want to remove the world if it is part of a name. This is not perfect
         * as a typo that leaves out the space before a world will be removed, but that should be
         * unlikely, partly because people rarely spell out worlds correctly.
         */
        private fun cleanUpMsg(options: ParseOptions, msg: String, fullName: String): String {
            // TODO: we should remove the user name if it starts the message
            var result = msg
            var changed = true
            while (changed) {
                changed = false
                options.dataCenter.servers.forEach { world ->
                    val index = result.indexOf(world)
                    if (index != -1) {
                        if (index > 0) {
                            if (result[index - 1] != ' ') {
                                result = result.removeRange(index, index + world.length)
                                changed = true
                            }
                        }
                    }
                }
            }
            result = result.removePrefix(fullName)
            result = result.trim()
            return result
        }

        /**
         * Cleans up the name. We remove any of the high Unicode values because they don't display
         * correctly. If the name is recognized, we convert it to a short form.
         *
         * NOTE: this may have to be revisited in asian countries.
         *
         * TODO: maybe it would be better to just remove the known offending characters or possibly
         *     from the one plane that they are in. I believe it is a user plane.
         */
        private fun cleanUpName(options: ParseOptions, name: String): String {
            var result = name
            if (result.isNotEmpty()) {
                if (result[0].code > 0x1000) {
                    result = result.substring(1)
                }
                options.dataCenter.servers.forEach { world ->
                    if (result.endsWith(world)) {
                        result = result.substring(0, result.length - world.length)
                    }
                }
            }
            result = result.trim()
            return result
        }

        /**
         * This "formatter" is used only to parse the timestamp from the log file which has a
         * specific format.
         */
        private val timestampParser = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter()
    }
}