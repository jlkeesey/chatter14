package pub.carkeys.logparse

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

enum class ChatType {
    CHAT, EMOTE, OTHER
}

data class ChatInfo(
    val lineNumber: Int, val name: String, val type: ChatType, val msg: String, val timestamp: OffsetDateTime
) {
    val shortName: String = when (name) {
        FULL_AELYM   -> SHORT_AELYM
        FULL_FIORA   -> SHORT_FIORA
        FULL_TIFAA_L -> SHORT_TIFAA_L
        FULL_TIFAA_S -> SHORT_TIFAA_S
        else         -> name
    }

    val typeName: String
        get() = when (type) {
            ChatType.CHAT  -> "C"
            ChatType.EMOTE -> "E"
            else           -> "?"
        }

    companion object {
        fun create(
            lineNumber: Int, name: String, type: String, msg: String, timestamp: String
        ): ChatInfo {
            val cleanName = cleanUpName(name)
            return ChatInfo(
                lineNumber = lineNumber,
                name = cleanName,
                type = parseType(type, cleanName),
                msg = cleanUpMsg(msg),
                timestamp = parseTimestamp(timestamp)
            )
        }

        private fun parseTimestamp(timestamp: String): OffsetDateTime {
            return OffsetDateTime.parse(timestamp, timestampParser)
        }

        private fun parseType(s: String, name: String): ChatType {
            if (name.isEmpty()) {
                return ChatType.OTHER
            }
            return when (s) {
                CODE_CHAT1, CODE_CHAT2 -> ChatType.CHAT
                CODE_EMOTE             -> ChatType.EMOTE
                else                   -> ChatType.OTHER
            }
        }

        private fun cleanUpMsg(msg: String): String {
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
            return result
        }

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
            return result
        }

        private val timestampParser = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter()

        private const val CODE_CHAT1 = "000E"
        private const val CODE_CHAT2 = "0011"
        private const val CODE_EMOTE = "001D"

        const val FULL_AELYM = "Aelym Sidrasylan"
        const val FULL_FIORA = "Fiora Greyback"
        const val FULL_TIFAA_S = "Tifaa Sidrasylan"
        const val FULL_TIFAA_L = "Tifaa Leonhart"

        private const val SHORT_AELYM = "Aelym"
        private const val SHORT_FIORA = "Fiora"
        private const val SHORT_TIFAA_S = "Tifaa"
        private const val SHORT_TIFAA_L = "Tifaa"

        @Suppress("SpellCheckingInspection")
        private val worlds = setOf(
            "Balmung",
            "Brynhildr",
            "Coeurl",
            "Diabolos",
            "Goblin",
            "Malboro",
            "Mateus",
        )
    }
}