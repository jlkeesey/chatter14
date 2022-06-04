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

enum class ParticipantType {
    PRIMARY, SECONDARY, ALL
}

data class ParseOptions(
    var dryRun: Boolean = false,
    var forceReplace: Boolean = false,
    var includeEmotes: Boolean = false,
    var group: ParseConfig.Group = ParseConfig.everyone,
    var participantType: ParticipantType = ParticipantType.PRIMARY,
    val files: MutableList<File> = mutableListOf(),
) {

    val codes: Set<ChatType>
        get() = if (includeEmotes) emoteCodes else chatCodes


    fun parseArgs(args: List<String>) {
        args.forEach { arg ->
            when (arg) {
                "-a" -> participantType = ParticipantType.ALL
                "-d" -> dryRun = true
                "-e" -> includeEmotes = true
                "-f" -> forceReplace = true
                "-s" -> participantType = ParticipantType.SECONDARY
                else -> {
                    if (arg[0] == '-') {
                        throw UsageException("Unknown flag: '$arg'")
                    }
                    files.add(File(arg))
                }
            }
        }
        if (files.size == 0) {
            throw UsageException("No files entered")
        }
    }

    companion object {
        private val primaryParticipants = setOf(ChatInfo.FULL_AELYM, ChatInfo.FULL_TIFAA_L, ChatInfo.FULL_TIFAA_S)

        private val secondaryParticipants: Set<String> = setOf(ChatInfo.FULL_FIORA, *primaryParticipants.toTypedArray())

        private val chatCodes = setOf(ChatType.CHAT)
        private val emoteCodes = setOf(ChatType.CHAT, ChatType.EMOTE)
    }
}
