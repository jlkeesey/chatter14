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
 * Contains the parsed information from a log line. This only captures chat and emote
 * types which the logs basically treat as the same.
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
         * This should be used to construct new ChatInfo objects as it massages the data
         * into a clean form.
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

        /**
         * Parses the timestamp string from the log.
         */
        private fun parseTimestamp(timestamp: String): OffsetDateTime {
            return OffsetDateTime.parse(timestamp, timestampParser)
        }

        /**
         * Parses the chat code type into the proper ChatCode. If the name is not a valid
         * user name then this is some sort of system generate message and should not be
         * handled as a user chat code.
         *
         * @param code the code to parse.
         * @param name the name to check for being a system message.
         * @return the corresponding ChatCode.
         */
        private fun parseCode(code: String, name: String): ChatCode {
            if (name.isEmpty()) {
                return ChatCode.OTHER
            }
            return ChatCode.fromCode(code)
        }

        /**
         * Cleans up the message. We remove any mention of a world as that is just messy.
         * We only remove the world if it is abutted to the previous word and that
         * signifies that it is part of a name. We only want to remove the world if it is
         * part of a name. This is not perfect as a typo that leaves out the space before
         * a world will be removed, but that should be unlikely, partly because people
         * rarely spell out worlds correctly.
         */
        private fun cleanUpMsg(options: ParseOptions, msg: String, fullName: String): String {
            var result = stripPrivateUse(msg)
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
         * Cleans up the name. If the name is recognized, we convert it to a short form.
         */
        private fun cleanUpName(options: ParseOptions, name: String): String {
            var result = stripPrivateUse(name)
            if (result.isNotEmpty()) {
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
         * Returns the given string with all characters from the lower private use are
         * removed. FFXIV uses the private use area for certain special characters that
         * are not part of the Unicode standard (exactly what the private use area is
         * for). However, these do not display properly one any other medium so we remove
         * them to prevent the output from looking messy.
         *
         * NOTE: currently we only strip out the lower private use block as FFXIV does not
         * seem to use the high block.
         */
        private fun stripPrivateUse(text: String): String {
            return text.filter { ch -> ch.code !in 0xE000..0xF8FF }
        }

        /**
         * This "formatter" is used only to parse the timestamp from the log file which
         * has a specific format.
         */
        private val timestampParser = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter()
    }
}