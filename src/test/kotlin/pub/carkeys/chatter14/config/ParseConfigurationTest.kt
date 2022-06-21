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

package pub.carkeys.chatter14.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ParseConfigurationTest {

    @Nested
    inner class ConfigObject {
        private val config = ParseConfiguration(
            dryRun = true,
            replaceIfExists = true,
            includeEmotes = false,
            performRename = false,
            dataCenterName = "Crystal",
            server = "Zalera",
            renames = mapOf(),
            groupList = listOf()
        )

        @Test
        fun getGroups() {
            val groups = config.groups

            groups.size shouldBe 1
            groups shouldContain (ParseConfiguration.everyone.shortName to ParseConfiguration.everyone)
        }

        @Test
        fun `validate success`() {
            config.validate()
        }

        @Test
        fun `validate missing server`() {
            val bad = config.copy(server = "Bobcat")
            shouldThrow<IllegalArgumentException> {
                bad.validate()
            }
        }

        @Test
        fun asOptions() {
            val options = config.asOptions()

            options shouldBe ParseOptions(
                dryRun = config.dryRun,
                forceReplace = config.replaceIfExists,
                includeEmotes = config.includeEmotes,
                dataCenter = config.dataCenter,
                renames = config.renames
            )
        }

        @Test
        fun write() {
            val builder = StringBuilder()
            config.write(builder)

            builder.toString() shouldBe """
                dryRun = true
                replaceIfExists = true
                includeEmotes = false
                performRename = false
                datacenter = "Crystal"
                server = "Zalera"
                
                [renames]

                """.trimIndent()
        }
    }
}