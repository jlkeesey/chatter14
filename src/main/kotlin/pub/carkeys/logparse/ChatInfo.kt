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

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Contains the parsed information from a log. This only captures chat and emote types which the logs basically
 * treat as the same.
 */
data class ChatInfo(
    val lineNumber: Int,
    val name: String,
    val shortName: String,
    val code: ChatCode,
    val msg: String,
    val timestamp: OffsetDateTime
) {
    val typeName: String
        get() = code.type.shortName

    companion object {
        /**
         * This should be used to construct new ChatInfo objects as it massages the data into a clean form.
         */
        fun create(
            options: ParseOptions, lineNumber: Int, name: String, code: String, msg: String, timestamp: String
        ): ChatInfo {
            val cleanName = cleanUpName(name)
            return ChatInfo(
                lineNumber = lineNumber,
                name = cleanName,
                shortName = options.renames[cleanName] ?: cleanName,
                code = parseCode(code, cleanName),
                msg = cleanUpMsg(msg, cleanName),
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
         * Cleans up the message. We remove any mention of a world as that is just messy.
         */
        private fun cleanUpMsg(msg: String, fullName: String): String {
            // TODO: we should remove the user name if it starts the message
            var result = msg
            var changed = true
            while (changed) {
                changed = false
                worlds.forEach { world ->
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
         * Cleans up the name. We remove any of the high Unicode values because they don't display correctly. If the
         * name is recognized, we convert it to a short form.
         */
        private fun cleanUpName(name: String): String {
            var result = name
            if (result.isNotEmpty()) {
                if (result[0].code > 0x1000) {
                    result = result.substring(1)
                }
                worlds.forEach { world ->
                    if (result.endsWith(world)) {
                        result = result.substring(0, result.length - world.length)
                    }
                }
            }
            result = result.trim()
            return result
        }

        /**
         * This "formatter" is used only to parse the timestamp from the log file which has a specific format.
         */
        private val timestampParser = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter()

        /**
         * These are all the worlds in the Crytal data center. We currently cannot get messaged from another data
         * center.
         */
        @Suppress("SpellCheckingInspection")
        private val worlds = setOf(
            "Balmung",
            "Brynhildr",
            "Coeurl",
            "Diabolos",
            "Goblin",
            "Malboro",
            "Mateus",
            "Zelera",
        )
    }
}