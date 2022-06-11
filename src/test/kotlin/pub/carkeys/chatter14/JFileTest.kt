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

import com.github.erikhuizinga.mockk.junit5.MockkClearUnmockExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

internal class JFileTest {

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class ConstructorTests {
        private val inputMock = mockk<File>()
        private val fileMock = mockk<File>()

        @BeforeEach
        fun setup() {
            every { inputMock.canonicalFile } returns fileMock
            every { inputMock.name } returns "/a/file/name"
        }

        @Test
        fun `constructor, file does not exist`() {
            every { fileMock.exists() } returns false

            shouldThrow<IllegalArgumentException> {
                JFile(inputMock)
            }
        }

        @Test
        fun `constructor, file is not a file`() {
            every { fileMock.exists() } returns true
            every { fileMock.isFile } returns false

            shouldThrow<IllegalArgumentException> {
                JFile(inputMock)
            }
        }

        @Test
        fun forEachLine() {
            val actionSlot = slot<(line: String) -> Unit>()

            every { fileMock.exists() } returns true
            every { fileMock.isFile } returns true

            mockkStatic(File::forEachLine)

            every { fileMock.forEachLine(charset = any(), action = capture(actionSlot)) } answers {
                actionSlot.captured.invoke("line 1")
                actionSlot.captured.invoke("line 2")
                actionSlot.captured.invoke("line 3")
            }

            val file = JFile(inputMock)

            val buffer = StringBuilder()
            var count = 0
            var lastLineNumber = -1
            file.forEach { lineNumber, text ->
                count++
                lastLineNumber = lineNumber
                if (lineNumber > 1) buffer.append(" ")
                buffer.append(text)
            }

            count shouldBe 3
            lastLineNumber shouldBe 3
            buffer.toString() shouldBe "line 1 line 2 line 3"
        }
    }
}