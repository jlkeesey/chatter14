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

import pub.carkeys.chatter14.ChatInfo
import pub.carkeys.chatter14.ParseOptions
import java.io.BufferedReader
import java.io.Reader

/**
 * Returns a sequence of ChatInfo objects from the target reader.
 */
fun Reader.readChats(options: ParseOptions): Sequence<ChatInfo> {
    val reader = this as? BufferedReader ?: BufferedReader(this)
    var lineNumber = 0
    return reader.lineSequence().map { line ->
        lineNumber++
        line.split("|")
    }.filter { parts -> parts[0] == "00" && parts.size >= 5 }.map { parts ->
        ChatInfo.create(
            options, lineNumber = lineNumber, name = parts[3], code = parts[2], msg = parts[4], timestamp = parts[1]
        )
    }
}

/**
 * Returns a sequence of filtered ChatInfo objects from the target reader. This filters for
 * the info objects that match the user's requirements.
 */
fun Reader.readLog(options: ParseOptions): Sequence<ChatInfo> {
    return this.readChats(options).filter { info ->
        options.types.contains(info.code.type) && options.group.matches(info.name)
    }
}
