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

import pub.carkeys.chatter14.ffxiv.ChatType
import pub.carkeys.chatter14.ffxiv.Universe

/**
 * The options to parse with.
 *
 * @property dryRun true if no output file should be generated.
 * @property forceReplace true if an existing output file should be replaced, otherwise it
 *     will be skipped.
 * @property includeEmotes true if emote "chat" lines should be included in the output.
 * @property dataCenter the data center this chat is from. Used to process server names.
 * @property group what lines should be kept in the output. This is usually a list of
 *     users whose chats are desired.
 * @property renames any username renames, usually used to shorten the names of common
 *     users.
 */
data class ParseOptions(
    var windowed: Boolean = false,
    var dryRun: Boolean = false,
    var forceReplace: Boolean = false,
    var includeEmotes: Boolean = false,
    var me: String = "Me",
    val dataCenter: Universe.DataCenter = Universe.DEFAULT,
    var group: ParseConfiguration.Group = ParseConfiguration.everyone,
    val renames: Map<String, String> = mutableMapOf(),
) {

    /**
     * Returns the ChatTypes to include in the output based on the settings.
     */
    val types: Set<ChatType>
        get() = if (includeEmotes) emoteTypes else chatTypes

    companion object {
        private val chatTypes = setOf(ChatType.CHAT)
        private val emoteTypes = setOf(ChatType.CHAT, ChatType.EMOTE)
    }
}
