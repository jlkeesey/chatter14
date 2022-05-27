package pub.carkeys.logparse

import java.io.File
import java.io.FileWriter
import java.lang.Integer.max
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import kotlin.io.path.forEachDirectoryEntry

class LogParse(args: Array<String>) {
    private var forceReplace = false
    private var shouldProcessAll = false
    private var shouldProcessEmotes = true
    private var participants = primaryParticipants
    private val filenames = mutableListOf<String>()
    private val codes: Set<ChatType>

    init {
        args.forEach { arg ->
            when (arg) {
                "-a" -> shouldProcessAll = true
                "-e" -> shouldProcessEmotes = true
                "-f" -> forceReplace = true
                "-s" -> participants = secondaryParticipants
                else -> {
                    if (arg[0] == '-') {
                        throw UsageException()
                    }
                    filenames.add(arg)
                }
            }
        }
        if (filenames.size == 0) {
            throw UsageException()
        }
        codes = if (shouldProcessEmotes) setOf(ChatType.CHAT, ChatType.EMOTE) else setOf(ChatType.CHAT)
    }

    fun process() {
        filenames.forEach { filename ->
            val current = File(filename)
            if (current.isFile) {
                processFile(current)
            } else if (current.isDirectory) {
                current.toPath().forEachDirectoryEntry("*.log") { p ->
                    val file = p.toFile()
                    if (file.isFile) {
                        processFile(file)
                    }
                }
            } else {
                current.parentFile.toPath().forEachDirectoryEntry(current.name) { p ->
                    val file = p.toFile()
                    if (file.isFile) {
                        processFile(file)
                    }
                }
            }
        }
    }

    private fun processFile(file: File) {
        val chatLog = parseFile(file)
        val canonicalFile = file.canonicalFile
        val filename = canonicalFile.nameWithoutExtension
        val newFile = File(canonicalFile.parentFile, "$filename.txt")
        writeFile(newFile, chatLog)
    }

    private fun writeFile(file: File, chatLog: List<ChatInfo>) {
        if (!forceReplace && file.exists()) {
            System.err.println("Target file exists, skipping: '${file.canonicalPath}")
            return
        }
        FileWriter(file).use { writer ->
            var nameMax = 0
            chatLog.forEach { info ->
                nameMax = max(nameMax, info.shortName.length)
            }

            val formatString = "%s %s %${nameMax}.${nameMax}s: %s\n"

            chatLog.forEach { info ->
                val message = String.format(
                    formatString, timestampFormatter.format(info.timestamp), info.typeName, info.name, info.msg
                )
                writer.write(message)
                print(message)
            }
        }
    }

    private fun parseFile(file: File): List<ChatInfo> {
        val chatLog = mutableListOf<ChatInfo>()
        JFile(file).forEach { lineNumber, line ->
            val parts = line.split("|")
            if (parts[0] == "00" && parts.size >= 5) {
                val info = ChatInfo.create(
                    lineNumber = lineNumber, name = parts[3], type = parts[2], msg = parts[4], timestamp = parts[1]
                )
                if (codes.contains(info.type)) {
                    if (shouldProcessAll || participants.contains(info.name)) {
                        chatLog.add(info)
                    }
                }
            }
        }
        return chatLog
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        private val primaryParticipants = setOf(ChatInfo.FULL_AELYM, ChatInfo.FULL_TIFAA_L, ChatInfo.FULL_TIFAA_S)

        @Suppress("SpellCheckingInspection")
        private val secondaryParticipants: Set<String> = setOf(ChatInfo.FULL_FIORA, *primaryParticipants.toTypedArray())

        //private val codes = setOf(ChatInfo.CODE_CHAT)

        private val timestampFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter()

    }
}