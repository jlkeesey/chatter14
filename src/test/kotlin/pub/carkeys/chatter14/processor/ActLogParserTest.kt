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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pub.carkeys.chatter14.config.ParseConfiguration
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.ffxiv.ChatInfo
import java.io.StringReader

internal class ActLogParserTest {
    val parser = ActLogParser()
    val defaultOptions = ParseOptions()
    val groupMatches = ParseConfiguration.GroupEntry("Bang Bang", "bang", listOf("Shooty McShootFace"))
    val groupNotMatches = ParseConfiguration.GroupEntry("Bang Bang", "bang", listOf("Wilburforce Glamtree"))

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class Simple {
        @Test
        fun `simple chat`() {
            val reader =
                StringReader("00|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace|Yellin' stuff|e6ac4fc48a7cd47d")

            val logs = parser.readLog(reader, defaultOptions)

            logs.size shouldBe 1
        }

        @Test
        fun `emote, not requested`() {
            val reader =
                StringReader("00|2022-06-04T12:58:09.0000000-07:00|001D|Shooty McShootFace|You blow a kiss.|ef07ae355655e6d3")

            val logs = parser.readLog(reader, defaultOptions)

            logs.isEmpty() shouldBe true
        }

        @Test
        fun `emote, requested`() {
            val reader =
                StringReader("00|2022-06-04T12:58:09.0000000-07:00|001D|Shooty McShootFace|You blow a kiss.|ef07ae355655e6d3")
            val options = defaultOptions.copy(includeEmotes = true)

            val logs = parser.readLog(reader, options)

            logs.size shouldBe 1
            logs[0].lineNumber shouldBe 1
        }

        @Test
        fun `chat line, matches group`() {
            val reader =
                StringReader("00|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace|Yellin' stuff|e6ac4fc48a7cd47d")
            val options = defaultOptions.copy(group = groupMatches)

            val logs = parser.readLog(reader, options)

            logs.size shouldBe 1
        }

        @Test
        fun `chat line, does not match group`() {
            val reader =
                StringReader("00|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace|Yellin' stuff|e6ac4fc48a7cd47d")
            val options = defaultOptions.copy(group = groupNotMatches)

            val logs = parser.readLog(reader, options)

            logs.isEmpty() shouldBe true
        }

    }

    @ExtendWith(MockkClearUnmockExtension::class)
    @Nested
    inner class Empty {
        @Test
        fun `empty reader`() {
            val reader = StringReader("")

            val logs = mutableListOf<ChatInfo>()
            parser.readLog(reader, defaultOptions).toCollection(logs)

            logs.isEmpty() shouldBe true
        }

        @Test
        fun `invalid line`() {
            val reader = StringReader("Woof")

            val logs = mutableListOf<ChatInfo>()
            parser.readLog(reader, defaultOptions).toCollection(logs)

            logs.isEmpty() shouldBe true
        }

        @Test
        fun `invalid line, incorrect number of columns`() {
            val reader = StringReader("00|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace")

            val logs = mutableListOf<ChatInfo>()
            parser.readLog(reader, defaultOptions).toCollection(logs)

            logs.isEmpty() shouldBe true
        }

        @Test
        fun `valid line, not chat`() {
            val reader =
                StringReader("ef|2022-06-04T12:14:33.0000000-07:00|001E|Shooty McShootFace|Yellin' stuff|e6ac4fc48a7cd47d")

            val logs = mutableListOf<ChatInfo>()
            parser.readLog(reader, defaultOptions).toCollection(logs)

            logs.isEmpty() shouldBe true
        }
    }
}