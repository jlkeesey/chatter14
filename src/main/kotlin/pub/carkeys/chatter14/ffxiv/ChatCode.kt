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

package pub.carkeys.chatter14.ffxiv

/**
 * These are the secondary codes for each chat type in Final Fantasy 14. There are many
 * more than this but these are the ones that we are most interested in. All the system
 * messages have a type like for NPC conversations, notifications of teleport, novice
 * network, etc. These aren't "chat" types as most people consider it. If these are
 * desired the keep all messages option can be selected.
 */
enum class ChatCode(val code: String, val type: ChatType) {
    SAY("000A", ChatType.CHAT),
    YELL("001E", ChatType.CHAT),
    SHOUT("000B", ChatType.CHAT),
    TELL_TO("000C", ChatType.CHAT), // A tell to another player
    TELL_FROM("000D", ChatType.CHAT), // A tell from another player
    FREE_COMPANY("0018", ChatType.CHAT),
    PARTY("000E", ChatType.CHAT),
    ALLIANCE("000F", ChatType.CHAT),
    CWLS1("0025", ChatType.CHAT),
    CWLS2("0065", ChatType.CHAT),
    CWLS3("0066", ChatType.CHAT),
    CWLS4("0067", ChatType.CHAT),
    CWLS5("0068", ChatType.CHAT),
    CWLS6("0069", ChatType.CHAT),
    CWLS7("006A", ChatType.CHAT),
    CWLS8("006B", ChatType.CHAT),
    LINKSHELL1("0010", ChatType.CHAT),
    LINKSHELL2("0011", ChatType.CHAT),
    LINKSHELL3("0012", ChatType.CHAT),
    LINKSHELL4("0013", ChatType.CHAT),
    LINKSHELL5("0014", ChatType.CHAT),
    LINKSHELL6("0015", ChatType.CHAT),
    LINKSHELL7("0016", ChatType.CHAT),
    LINKSHELL8("0017", ChatType.CHAT),
    EMOTE("001D", ChatType.EMOTE),
    EMOTE_CUSTOM("001C", ChatType.EMOTE),
    OTHER("????", ChatType.OTHER);

    companion object {
        private val codeToChatCode = mapOf(
            SAY.code to SAY,
            YELL.code to YELL,
            SHOUT.code to SHOUT,
            TELL_TO.code to TELL_TO,
            TELL_FROM.code to TELL_FROM,
            FREE_COMPANY.code to FREE_COMPANY,
            PARTY.code to PARTY,
            ALLIANCE.code to ALLIANCE,
            CWLS1.code to CWLS1,
            CWLS2.code to CWLS2,
            CWLS3.code to CWLS3,
            CWLS4.code to CWLS4,
            CWLS5.code to CWLS5,
            CWLS6.code to CWLS6,
            CWLS7.code to CWLS7,
            CWLS8.code to CWLS8,
            LINKSHELL1.code to LINKSHELL1,
            LINKSHELL2.code to LINKSHELL2,
            LINKSHELL3.code to LINKSHELL3,
            LINKSHELL4.code to LINKSHELL4,
            LINKSHELL5.code to LINKSHELL5,
            LINKSHELL6.code to LINKSHELL6,
            LINKSHELL7.code to LINKSHELL7,
            LINKSHELL8.code to LINKSHELL8,
            EMOTE.code to EMOTE,
            EMOTE_CUSTOM.code to EMOTE_CUSTOM,
        )

        fun fromCode(code: String): ChatCode {
            return codeToChatCode[code] ?: OTHER
        }
    }
}
