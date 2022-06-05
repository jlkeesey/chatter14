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

data class ParseOptions(
    var dryRun: Boolean = false,
    var forceReplace: Boolean = false,
    var includeEmotes: Boolean = false,
    val dataCenter: DataCenter = DataCenter.DEFAULT,
    var group: ParseConfig.Group = ParseConfig.everyone,
    val renames: Map<String, String> = mutableMapOf(),
    val files: MutableList<File> = mutableListOf(),
) {

    val types: Set<ChatType>
        get() = if (includeEmotes) emoteTypes else chatTypes

    companion object {
        private val chatTypes = setOf(ChatType.CHAT)
        private val emoteTypes = setOf(ChatType.CHAT, ChatType.EMOTE)
    }
}
