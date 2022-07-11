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
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.ffxiv.ChatCode
import pub.carkeys.chatter14.ffxiv.ChatInfo
import java.io.StringReader
import java.io.StringWriter
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Suppress("SpellCheckingInspection")
internal class ActLogProcessorTest {
    private val clock = Clock.fixed(Instant.EPOCH, ZoneId.of("America/Los_Angeles"))
    private val parser = mockk<ActLogParser>()
    private val processor = ActLogProcessor(parser, clock)
    private val defaultOptions = ParseOptions()

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class Processor {
        @Test
        fun `log is empty`() {
            val reader = StringReader("")
            val writer = StringWriter(100)
            every { parser.readLog(any(), any()) } returns listOf()

            processor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                # No lines matched the criteria
            
            """.trimIndent()
        }

        @Test
        fun `one entry`() {
            val reader = StringReader("")
            val writer = StringWriter(100)
            every { parser.readLog(any(), any()) } returns listOf(
                ChatInfo(
                    1, SHOOTY, SHOOTY_SHORT, ChatCode.TELL, "Who's there?", OffsetDateTime.now(clock)
                )
            )

            processor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                1969-12-31 16:00:00 C Shooty: Who's there?
            
            """.trimIndent()
        }

        @Test
        fun `multiple entries`() {
            val reader = StringReader("")
            val writer = StringWriter(100)
            every { parser.readLog(any(), any()) } returns listOf(
                ChatInfo(
                    1, SHOOTY, SHOOTY_SHORT, ChatCode.TELL, "Who's there?", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    2, WILBUR, WILBUR_SHORT, ChatCode.TELL, "I am", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    3, SHOOTY, SHOOTY_SHORT, ChatCode.TELL, "Knock, knock", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    4, WILBUR, WILBUR_SHORT, ChatCode.TELL, "Nope, not doing this.", OffsetDateTime.now(clock)
                ),
            )

            processor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                1969-12-31 16:00:00 C Shooty: Who's there?
                1969-12-31 16:00:00 C Wilbur: I am
                1969-12-31 16:00:00 C Shooty: Knock, knock
                1969-12-31 16:00:00 C Wilbur: Nope, not doing this.
            
            """.trimIndent()
        }
    }

    companion object {
        private const val SHOOTY = "Shooty McShootFace"
        private const val SHOOTY_SHORT = "Shooty"
        private const val WILBUR = "Wilburforce Glamtree"
        private const val WILBUR_SHORT = "Wilbur"
    }
}