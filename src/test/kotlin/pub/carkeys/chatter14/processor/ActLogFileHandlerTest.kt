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

import com.github.erikhuizinga.mockk.junit5.MockkClearUnmockExtension
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pub.carkeys.chatter14.ParseOptions
import pub.carkeys.chatter14.log4j.UnitTestEventAppender
import java.io.File
import java.io.StringReader
import java.io.StringWriter

internal class ActLogFileHandlerTest {
    private val defaultOptions = ParseOptions()
    private val processor = mockk<ActLogProcessor>()
    private val fileManager = mockk<ChatterFileManager>()

    @BeforeEach
    fun setup() {
        UnitTestEventAppender.clear()
        val inputFileName = slot<String>()
        every { processor.process(name = capture(inputFileName), any(), any()) } just Runs
        every { fileManager.openForRead(any()) } returns StringReader("")
        every { fileManager.openForWrite(any()) } returns StringWriter(100)
    }

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class OneFile {
        private val file = mockk<File>()
        private val options = defaultOptions.copy(files = mutableListOf(file))
        private val newFile = mockk<File>()
        private val handler = ActLogFileHandler(
            options = options,
            processor = processor,
            fileManager = fileManager,
        )

        @BeforeEach
        fun setup() {
            every { file.isFile } returns true
            every { file.exists() } returns true
            every { file.path } returns FILEPATH
            every { newFile.path } returns NEW_FILEPATH
            every { fileManager.makeOutputFileName(any(), any()) } returns newFile
        }

        @Test
        fun `file exists, new file does not exist`() {
            every { newFile.exists() } returns false
            handler.process()
            verify(exactly = 1) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf("Processing $FILEPATH")
        }

        @Test
        fun `file exists, new file exists, force = false`() {
            every { newFile.exists() } returns true
            handler.process()
            verify(exactly = 0) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf("Target file exists, skipping: '$NEW_FILEPATH'")
        }

        @Test
        fun `file exists, new file exists, force = true`() {
            val forceHandler = ActLogFileHandler(
                options = options.copy(forceReplace = true),
                processor = processor,
                fileManager = fileManager,
            )

            every { newFile.exists() } returns true
            forceHandler.process()
            verify(exactly = 1) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf("Processing $FILEPATH")
        }

        @Test
        fun `file does not exist`() {
            val notExist = mockk<File>()
            every { notExist.isFile } returns true
            every { notExist.exists() } returns false
            every { notExist.path } returns "notExist"
            val options = defaultOptions.copy(files = mutableListOf(notExist))

            val handler = ActLogFileHandler(
                options = options,
                processor = processor,
                fileManager = fileManager,
            )
            handler.process()

            verify { processor wasNot Called }
            verify { fileManager wasNot Called }
            UnitTestEventAppender.messages shouldBe listOf("Input file notExist does not exist")
        }
    }

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class OneDirectory {
        private val file = mockk<File>()
        private val options = defaultOptions.copy(files = mutableListOf(file))
        private val newFile = mockk<File>()
        private val handler = ActLogFileHandler(
            options = options,
            processor = processor,
            fileManager = fileManager,
        )

        @BeforeEach
        fun setup() {
            every { file.isFile } returns false
            every { file.isDirectory } returns true
            every { file.exists() } returns true
            every { file.path } returns PATH

            every { newFile.path } returns NEW_FILEPATH
            every { fileManager.makeOutputFileName(any(), any()) } returns newFile
        }

        @Test
        fun `directory exists, no files`() {
            every { fileManager.forEachFile(any(), any(), any()) } just Runs

            handler.process()

            verify(exactly = 0) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf("Processing all log files in $PATH")
        }

        @Test
        fun `directory exists, one file does not exists`() {
            val first = mockk<File>()
            every { first.exists() } returns false
            every { first.path } returns "$PATH/first.log"
            val action = slot<(File) -> Unit>()
            every { fileManager.forEachFile(any(), any(), action = capture(action)) } answers {
                action.captured(first)
            }

            handler.process()

            verify(exactly = 0) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf(
                "Processing all log files in $PATH", "   Input file $PATH/first.log does not exist"
            )
        }

        @Test
        fun `directory exists, one file, exists but is not a file`() {
            val first = mockk<File>()
            every { first.exists() } returns true
            every { first.isFile } returns false
            every { first.path } returns "$PATH/first.log"
            val action = slot<(File) -> Unit>()
            every { fileManager.forEachFile(any(), any(), action = capture(action)) } answers {
                action.captured(first)
            }

            handler.process()

            verify(exactly = 0) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf(
                "Processing all log files in $PATH", "   Input name /a/path/first.log is not a file"
            )
        }

        @Test
        fun `directory exists, one file, exists and is a file`() {
            every { newFile.exists() } returns false
            val first = mockk<File>()
            every { first.exists() } returns true
            every { first.isFile } returns true
            every { first.path } returns "$PATH/first.log"
            val action = slot<(File) -> Unit>()
            every { fileManager.forEachFile(any(), any(), action = capture(action)) } answers {
                action.captured(first)
            }

            handler.process()

            verify(exactly = 1) { processor.process(any(), any(), any()) }
            UnitTestEventAppender.messages shouldBe listOf(
                "Processing all log files in $PATH", "   Processing $PATH/first.log",
            )
        }
    }

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class Basic {
        @Test
        fun `no files`() {
            val handler = ActLogFileHandler(
                options = defaultOptions,
                processor = processor,
                fileManager = fileManager,
            )

            handler.process()

            verify { processor wasNot Called }
            verify { fileManager wasNot Called }
            UnitTestEventAppender.messages shouldBe listOf("No files to process")
        }
    }

    companion object {
        private const val ROOT = "/a"
        private const val DIRECTORY = "path"
        private const val PATH = "$ROOT/$DIRECTORY"
        private const val FILENAME = "woof.log"
        private const val NEW_FILENAME = "woof-everyone.log"
        private const val FILEPATH = "$PATH/$FILENAME"
        private const val NEW_FILEPATH = "$PATH/$NEW_FILENAME"
    }
}