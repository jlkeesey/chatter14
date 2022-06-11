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

package pub.carkeys.chatter14

import java.io.File

/**
 * Simple file reader.
 */
class JFile(path: File) {
    private val file: File = path.canonicalFile

    init {
        if (!file.exists()) {
            throw IllegalArgumentException("'${path.name}' does not exist")
        }
        if (!file.isFile) {
            throw IllegalArgumentException("'${path.name}' is not a file")
        }
    }

    fun forEach(action: (Int, String) -> Unit) {
        var lineNumber = 1
        file.forEachLine { line ->
            action(lineNumber, line)
            lineNumber++
        }
    }
}