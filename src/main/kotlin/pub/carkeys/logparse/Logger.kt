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

import java.io.PrintWriter
import java.io.Writer

class Logger {
    private val stdout = PrintWriter(System.out)
    private val stderr = PrintWriter(System.err)

    private var messageWriter = LogWriter { s -> stdout.print(s) }
    private var errorWriter = LogWriter { s -> stderr.print(s) }

    fun message(msg: String) {
        messageWriter.write(msg)
    }

    fun error(msg: String) {
        errorWriter.write(msg)
    }

    fun messageWriter(): Writer = messageWriter

    fun setMessenger(message: Messenger) {
        flush()
        messageWriter = LogWriter { s -> message.message(s) }
        errorWriter = LogWriter { s -> message.error(s) }
    }

    fun flush() {
        messageWriter.flush()
        errorWriter.flush()
    }

    private class LogWriter(private val out: (String) -> Unit) : Writer() {
        private val builder = StringBuilder()

        override fun close() {
            flush()
        }

        override fun flush() {
            if (builder.isNotEmpty()) {
                if (builder[builder.length - 1] != '\n') {
                    builder.append("\n")
                }
                out(builder.toString())
                builder.clear()
            }
        }

        override fun write(cbuf: CharArray, off: Int, len: Int) {
            val end = off + len - 1
            for (i in off..end) {
                builder.append(cbuf[i])
                if (cbuf[i] == '\n') {
                    flush()
                }
            }
        }
    }
}