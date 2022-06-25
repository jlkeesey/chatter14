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

import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.logger
import java.io.File

/**
 * The ACT log processor. This reads each log file in turn, filters for the requested items,
 * and then writes them to a file.
 */
class ActLogFileHandler(
    private val processor: ActLogProcessor = ActLogProcessor(),
    private val fileManager: ChatterFileManager = ChatterFileManager(),
) {
    /*
     * Process each of the files in the options. If the name is a file we just process
     * that file. If the name is a directory, we process all .log files in that directory.
     * Otherwise, we assume that the name is a globbing spec and process each file that matches.
     */
    fun process(options: ParseOptions, files: List<File>) {
        if (files.isEmpty()) {
            logger.warn("No files to process")
        }
        files.forEach { current ->
            if (current.isFile) {
                processFile(options, current)
            } else if (current.isDirectory) {
                logger.info("Processing all log files in ${current.path}")
                fileManager.forEachFile(current, "*.log") { processFile(options, it, indent = "   ") }
            } else {
                fileManager.forEachFile(current.parentFile, current.name) { processFile(options, it, indent = "   ") }
            }
        }
    }

    private fun processFile(options: ParseOptions, inputFile: File, indent: String = "") {
        if (!inputFile.exists()) {
            logger.warn("${indent}Input file ${inputFile.path} does not exist")
            return
        }
        if (!inputFile.isFile) {
            logger.warn("${indent}Input name ${inputFile.path} is not a file")
            return
        }
        val outputFile = fileManager.makeOutputFileName(inputFile, options.group.shortName)
        if (!options.forceReplace && outputFile.exists()) {
            logger.warn("${indent}Target file exists, skipping: '${outputFile.path}'")
            return
        }
        fileManager.openForRead(inputFile).use { reader ->
            fileManager.openForWrite(outputFile).use { writer ->
                logger.info("${indent}Processing ${inputFile.path}")
                processor.process(inputFile.path, options, reader, writer)
            }
        }
    }

    companion object {
        private val logger by logger()
    }
}