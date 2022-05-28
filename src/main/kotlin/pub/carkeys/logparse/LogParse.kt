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

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Writer
import java.lang.Integer.max
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import kotlin.io.path.forEachDirectoryEntry

/**
 * The log processor. This reads each file in turn, filters for the requested items, and then writes them to a file.
 */
class LogParse(private val options: ParseOptions) {
    /**
     * Process each of the files on the command line. If the name is a file we just process that file. If the name is
     * a directory, we process all .log files in that directory. Otherwise, we assume that the name is a globbing
     * spec and process each file that matches.
     */
    fun process() {
        options.files.forEach { current ->
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

    /**
     * Process one file.
     */
    private fun processFile(file: File) {
        println("Processing ${file.name} ...")
        val chatLog = parseFile(file)
        val newFile = makeNewFile(file)
        writeFile(newFile, chatLog)
    }

    /**
     * Writes the filtered contents to the target file. We fail if the target already exists unless the force
     * flag was supplied.
     */
    private fun writeFile(file: File, chatLog: List<ChatInfo>) {
        if (chatLog.isEmpty()) return

        if (!options.forceReplace && file.exists()) {
            System.err.println("Target file exists, skipping: '${file.canonicalPath}")
            return
        }

        val nameMax = chatLog.map { it.shortName.length }.reduce { lhs, rhs -> max(lhs, rhs) }

        if (options.dryRun) {
            writeOut(PrintWriter(System.out), chatLog, nameMax)
        } else {
            FileWriter(file).use { writer -> writeOut(writer, chatLog, nameMax) }
        }
    }

    /**
     * Writes the chat log to the given Writer.
     */
    private fun writeOut(writer: Writer, chatLog: List<ChatInfo>, nameMax: Int) {
        val formatString = "%s %s %${nameMax}.${nameMax}s: %s\n"
        chatLog.forEach { info ->
            val message = String.format(
                formatString, timestampFormatter.format(info.timestamp), info.typeName, info.name, info.msg
            )
            writer.write(message)
        }
    }

    /**
     * Reads the given file and returns a list of the filtered items.
     */
    private fun parseFile(file: File): List<ChatInfo> {
        val chatLog = mutableListOf<ChatInfo>()
        JFile(file).forEach { lineNumber, line ->
            val parts = line.split("|")
            if (parts[0] == "00" && parts.size >= 5) {
                val info = ChatInfo.create(
                    lineNumber = lineNumber, name = parts[3], type = parts[2], msg = parts[4], timestamp = parts[1]
                )
                if (options.codes.contains(info.type)) {
                    if (options.participantType == ParticipantType.ALL || options.participants.contains(info.name)) {
                        chatLog.add(info)
                    }
                }
            }
        }
        return chatLog
    }

    /**
     * Creates the new file name from the existing one.
     */
    private fun makeNewFile(file: File): File {
        val canonicalFile = file.canonicalFile
        val filename = canonicalFile.nameWithoutExtension
        return File(canonicalFile.parentFile, "$filename.txt")
    }

    companion object {
        private val timestampFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter()
    }
}