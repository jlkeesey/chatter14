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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

internal class ChatInfoTest {
    private val defaultOptions = ParseOptions()
    private val expectedTimestamp = OffsetDateTime.of(2022, 6, 4, 12, 17, 32, 0, ZoneOffset.ofHours(-7))
    private val stringTimestamp = "2022-06-04T12:17:32.0000000-07:00"
    private val expectedName = "Shooty McShootFace"
    private val expectedCode = ChatCode.TELL
    private val expectedLinenumber = 1745
    private val expectedMessage = "Say something"
    private val expectedChatInfo = ChatInfo(
        expectedLinenumber, expectedName, expectedName, expectedCode, expectedMessage, expectedTimestamp
    )

    @Nested
    inner class Name {
        @Test
        fun `basic name`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = expectedName,
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }

        @Test
        fun `basic name with world`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = "${expectedName}Goblin",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }

        @Test
        fun `basic name with separate world`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = "$expectedName Goblin",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }

        @Test
        fun `basic name with high character`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = "\uE090$expectedName",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }
    }

    @Nested
    inner class ShortName {
        private val options = defaultOptions.copy(renames = mapOf(expectedName to "Bob"))

        @Test
        fun `basic shortening`() {
            val actual = ChatInfo.create(
                options = options,
                lineNumber = expectedLinenumber,
                name = expectedName,
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(shortName = "Bob")
        }

        @Test
        fun `basic name no rename match`() {
            val actual = ChatInfo.create(
                options = options,
                lineNumber = expectedLinenumber,
                name = "${expectedName}xxx",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(name = "${expectedName}xxx", shortName = "${expectedName}xxx")
        }

        @Test
        fun `basic name with world`() {
            val actual = ChatInfo.create(
                options = options,
                lineNumber = expectedLinenumber,
                name = "${expectedName}Goblin",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(shortName = "Bob")
        }

        @Test
        fun `basic name with separate world`() {
            val actual = ChatInfo.create(
                options = options,
                lineNumber = expectedLinenumber,
                name = "$expectedName Goblin",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(shortName = "Bob")
        }

        @Test
        fun `basic name with high character`() {
            val actual = ChatInfo.create(
                options = options,
                lineNumber = expectedLinenumber,
                name = "\uE090$expectedName",
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(shortName = "Bob")
        }
    }

    @Nested
    inner class Code {
        @Test
        fun `valid code`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = expectedName,
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }

        @Test
        fun `invalid code`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                code = "XXX",
                lineNumber = expectedLinenumber,
                msg = expectedMessage,
                name = expectedName,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo.copy(code = ChatCode.OTHER)
        }
    }


    @Nested
    inner class Timestamp {
        @Test
        fun `valid timestamp`() {
            val actual = ChatInfo.create(
                options = defaultOptions,
                lineNumber = expectedLinenumber,
                name = expectedName,
                code = expectedCode.code,
                msg = expectedMessage,
                timestamp = stringTimestamp,
            )

            actual shouldBe expectedChatInfo
        }

        @Test
        fun `invalid timestamp`() {
            shouldThrow<DateTimeParseException> {
                ChatInfo.create(
                    timestamp = stringTimestamp + "xxx",
                    options = defaultOptions,
                    code = expectedCode.code,
                    lineNumber = expectedLinenumber,
                    msg = expectedMessage,
                    name = expectedName
                )
            }
        }
    }
}