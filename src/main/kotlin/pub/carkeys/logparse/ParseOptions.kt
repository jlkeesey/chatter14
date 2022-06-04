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
    var group: ParseConfig.Group = ParseConfig.everyone,
    val files: MutableList<File> = mutableListOf(),
) {

    val codes: Set<ChatType>
        get() = if (includeEmotes) emoteCodes else chatCodes

    companion object {
//        private val primaryParticipants = setOf(ChatInfo.FULL_AELYM, ChatInfo.FULL_TIFAA_L, ChatInfo.FULL_TIFAA_S)
//
//        private val secondaryParticipants: Set<String> = setOf(ChatInfo.FULL_FIORA, *primaryParticipants.toTypedArray())

        private val chatCodes = setOf(ChatType.CHAT)
        private val emoteCodes = setOf(ChatType.CHAT, ChatType.EMOTE)
    }
}
