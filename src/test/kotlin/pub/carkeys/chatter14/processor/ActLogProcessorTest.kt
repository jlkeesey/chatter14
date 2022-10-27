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
                    1, SHOOTY, SHOOTY_SHORT, ChatCode.TELL_TO, "Who's there?", OffsetDateTime.now(clock)
                )
            )

            processor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                1969-12-31 16:00:00 C Shooty: Who's there?
            
            """.trimIndent()
        }

        @Test
        fun `one entry data massaged`() {
            val reader = StringReader("")
            val writer = StringWriter(100)
            every { parser.readLog(any(), any()) } returns listOf(
                ChatInfo.create(
                    options = defaultOptions,
                    lineNumber = 1,
                    name = SHOOTY_SHORT,
                    code = ChatCode.TELL_TO.code,
                    msg = "Who's there?",
                    timestamp = "1969-12-31T16:00-08:00"
                )
            )

            processor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                1969-12-31 16:00:00 C Me: Who's there?
            
            """.trimIndent()
        }

        @Test
        fun `multiple entries`() {
            val reader = StringReader("")
            val writer = StringWriter(100)
            every { parser.readLog(any(), any()) } returns listOf(
                ChatInfo(
                    1, SHOOTY, SHOOTY_SHORT, ChatCode.TELL_TO, "Who's there?", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    2, WILBUR, WILBUR_SHORT, ChatCode.TELL_TO, "I am", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    3, SHOOTY, SHOOTY_SHORT, ChatCode.TELL_TO, "Knock, knock", OffsetDateTime.now(clock)
                ),
                ChatInfo(
                    4, WILBUR, WILBUR_SHORT, ChatCode.TELL_TO, "Nope, not doing this.", OffsetDateTime.now(clock)
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

        @Test
        fun `full list`() {
            val reader = StringReader(
                """
                00|2022-06-04T12:14:25.0000000-07:00|000A|Shooty McShootFace|Sayin' stuff|38d584c2f1be915d
                00|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace|Yellin' stuff|e6ac4fc48a7cd47d
                00|2022-06-04T12:14:41.0000000-07:00|000B|Shooty McShootFace|Shoutin' stuff|52b754de3daac69c
                00|2022-06-04T12:53:53.0000000-07:00|0018|Shooty McShootFace|Free Company stuff|b8e0d3657bfa0f1a
                00|2022-06-04T12:15:14.0000000-07:00|000E|Shooty McShootFace|Party stuff|272c59820444470f
                00|2022-06-06T00:54:11.0000000-07:00|000F|Shooty McShootFace|Alliance stuff|6704c3b083279ed5
                00|2022-06-04T12:15:32.0000000-07:00|000C|Shooty McShootFace|Tellin' stuff|05fab732baca3fb8
                00|2022-06-04T12:15:32.0000000-07:00|000D|Shooty McShootFace|Being told stuff|05fab732bfca3fb8
                00|2022-06-04T12:48:34.0000000-07:00|0025|Shooty McShootFace|CWLS1: Oops, wrong chat :D|5ccebff00132d1f1
                00|2022-06-04T12:17:32.0000000-07:00|0065|Shooty McShootFace|CWLS2: CWLS stuff|78d3d98825af4397
                00|2022-06-04T13:04:53.0000000-07:00|0066|Shooty McShootFace|CWLS3: stuff|02fadbe2ebb00c8e
                00|2022-06-04T13:04:53.0000000-07:00|0067|Shooty McShootFace|CWLS4: stuff|02fadbe2ebb00c8f
                00|2022-06-04T13:04:53.0000000-07:00|0068|Shooty McShootFace|CWLS5: stuff|02fadbe2ebb00c90
                00|2022-06-04T13:04:53.0000000-07:00|0069|Shooty McShootFace|CWLS6: stuff|02fadbe2ebb00c91
                00|2022-06-04T13:04:53.0000000-07:00|006A|Shooty McShootFace|CWLS7: stuff|02fadbe2ebb00c92
                00|2022-06-04T13:04:53.0000000-07:00|006B|Shooty McShootFace|CWLS8: stuff|02fadbe2ebb00c93
                00|2022-06-04T12:19:21.0000000-07:00|0039||Triple Triad matches allowed in current area.|909bdfb481cccd0f
                00|2022-06-04T12:20:13.0000000-07:00|0039||You have left the sanctuary.|65a6992a945e26cc
                00|2022-06-04T12:22:20.0000000-07:00|003D|Ninisha|One personal linkshell, coming right up! What name would you like to register?|99db7fc1f67e3ba6
                00|2022-06-04T12:22:39.0000000-07:00|0039||You create the linkshell “A New Linkshell.”|df522de385ac0bbe
                00|2022-06-04T12:22:40.0000000-07:00|003D|Ninisha|Congratulations! You are now the proud owner of your very own linkshell.|5d59ed8e69f32e88
                00|2022-06-04T12:22:49.0000000-07:00|2239||Linkshell invite sent to Marvin Martian.|54a37f7cb333c0c8
                00|2022-06-04T12:23:12.0000000-07:00|2239||Marvin Martian has joined “A New Linkshell.”|1920af070acd7c6a
                00|2022-06-04T12:45:14.0000000-07:00|0010|Shooty McShootFace|Linkshell 1 stuff|0e280b8fcb3d95d6
                00|2022-06-04T12:45:14.0000000-07:00|0011|Shooty McShootFace|Linkshell 2 stuff|0e280b8fcb3d95d6
                00|2022-06-04T12:23:40.0000000-07:00|0012|Shooty McShootFace|Linkshell 3 stuff|0e280b8fcb3d95d7
                00|2022-06-04T12:23:40.0000000-07:00|0013|Shooty McShootFace|Linkshell 4 stuff|0e280b8fcb3d95d8
                00|2022-06-04T12:23:40.0000000-07:00|0014|Shooty McShootFace|Linkshell 5 stuff|0e280b8fcb3d95d9
                00|2022-06-04T12:23:40.0000000-07:00|0015|Shooty McShootFace|Linkshell 6 stuff|0e280b8fcb3d95da
                00|2022-06-04T12:23:40.0000000-07:00|0016|Shooty McShootFace|Linkshell 7 stuff|0e280b8fcb3d95db
                00|2022-06-04T12:23:40.0000000-07:00|0017|Shooty McShootFace|Linkshell 8 stuff|0e280b8fcb3d95dc
                00|2022-06-04T12:19:00.0000000-07:00|082B||You use Return.|31cfcfa9902ed589
                00|2022-06-04T12:58:09.0000000-07:00|001D|Shooty McShootFace|You blow a kiss.|ef07ae355655e6d3
                00|2022-06-04T12:59:00.0000000-07:00|001C|Shooty McShootFace| custom emote stuff|48d41ec797be2b97
                03|2022-05-22T21:05:38.4830000-07:00|40000913|Striking Dummy|00|50|0000|00||541|10183|2134350|2134350|0|10000|||-647.54|-665.49|26.00|-1.57|650d954cc72a2b75
                03|2022-05-22T21:05:38.4830000-07:00|4000090D|Striking Dummy|00|1|0000|00||541|901|44|44|0|10000|||-670.20|-636.40|20.07|-1.83|5fe02f93abfb82b0
                03|2022-05-22T21:05:38.4830000-07:00|4000091E|Striking Dummy|00|32|0000|00||541|901|2589|2589|0|10000|||-658.65|-701.07|25.00|-1.57|8bb2dadcebade32e
            """.trimIndent()
            )
            val writer = StringWriter(1000)
            val fParser = ActLogParser()
            val fProcessor = ActLogProcessor(fParser, clock)

            fProcessor.process("woof.log", defaultOptions, reader, writer)

            writer.toString() shouldBe """
                # Created at 1969-12-31T16:00-08:00
                2022-06-04 12:14:25 C Shooty McShootFace: Sayin' stuff
                2022-06-04 12:14:33 C Shooty McShootFace: Yellin' stuff
                2022-06-04 12:14:41 C Shooty McShootFace: Shoutin' stuff
                2022-06-04 12:53:53 C Shooty McShootFace: Free Company stuff
                2022-06-04 12:15:14 C Shooty McShootFace: Party stuff
                2022-06-06 00:54:11 C Shooty McShootFace: Alliance stuff
                2022-06-04 12:15:32 C Me                : Tellin' stuff
                2022-06-04 12:15:32 C Shooty McShootFace: Being told stuff
                2022-06-04 12:48:34 C Shooty McShootFace: CWLS1: Oops, wrong chat :D
                2022-06-04 12:17:32 C Shooty McShootFace: CWLS2: CWLS stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS3: stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS4: stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS5: stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS6: stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS7: stuff
                2022-06-04 13:04:53 C Shooty McShootFace: CWLS8: stuff
                2022-06-04 12:45:14 C Shooty McShootFace: Linkshell 1 stuff
                2022-06-04 12:45:14 C Shooty McShootFace: Linkshell 2 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 3 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 4 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 5 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 6 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 7 stuff
                2022-06-04 12:23:40 C Shooty McShootFace: Linkshell 8 stuff
            
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