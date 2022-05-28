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
    var shouldProcessEmotes: Boolean = false,
    var participantType: ParticipantType = ParticipantType.PRIMARY,
    val files: MutableList<File> = mutableListOf(),
) {
    val participants: Set<String>
        get() = when (participantType) {
            ParticipantType.PRIMARY   -> primaryParticipants
            ParticipantType.SECONDARY -> secondaryParticipants
            ParticipantType.ALL       -> setOf()
        }

    val codes: Set<ChatType>
        get() = if (shouldProcessEmotes) emoteCodes else chatCodes

    companion object {
        private val primaryParticipants = setOf(ChatInfo.FULL_AELYM, ChatInfo.FULL_TIFAA_L, ChatInfo.FULL_TIFAA_S)

        private val secondaryParticipants: Set<String> = setOf(ChatInfo.FULL_FIORA, *primaryParticipants.toTypedArray())

        private val chatCodes = setOf(ChatType.CHAT)
        private val emoteCodes = setOf(ChatType.CHAT, ChatType.EMOTE)

        fun parseArgs(args: List<String>): ParseOptions {
            val options = ParseOptions()
            args.forEach { arg ->
                when (arg) {
                    "-a" -> options.participantType = ParticipantType.ALL
                    "-d" -> options.dryRun = true
                    "-e" -> options.shouldProcessEmotes = true
                    "-f" -> options.forceReplace = true
                    "-s" -> options.participantType = ParticipantType.SECONDARY
                    else -> {
                        if (arg[0] == '-') {
                            throw UsageException("Unknown flag: '$arg'")
                        }
                        options.files.add(File(arg))
                    }
                }
            }
            if (options.files.size == 0) {
                throw UsageException("No files entered")
            }
            return options
        }
    }
}
