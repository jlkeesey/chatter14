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

/**
 * Simplistic logger abstraction so that we can send the output to the command line or a Swing panel.
 */
class Logger {
    private val stdout = PrintWriter(System.out)
    private val stderr = PrintWriter(System.err)

    private var messageWriter = LogWriter { s -> stdout.print(s) }
    private var errorWriter = LogWriter { s -> stderr.print(s) }

    /**
     * Write a normal message to the log.
     */
    fun message(msg: String) {
        messageWriter.write(msg)
    }

    /**
     * Write an error message to the log.
     */
    fun error(msg: String) {
        errorWriter.write(msg)
    }

    /**
     * Return the normal message writer. Bit of a hack but I just didn't feel like doing any more work on this.
     */
    fun messageWriter(): Writer = messageWriter

    /**
     * Sets the output of this logger to the given Messager.
     */
    fun setMessenger(message: Messenger) {
        flush()
        messageWriter = LogWriter { s -> message.message(s) }
        errorWriter = LogWriter { s -> message.error(s) }
    }

    /**
     * Flushes all the outputs.
     */
    fun flush() {
        messageWriter.flush()
        errorWriter.flush()
    }

    /**
     * Simple Writer implementation to send the output where it should go and prevent close() from actually closing
     * anything.
     */
    private class LogWriter(private val out: (String) -> Unit) : Writer() {
        private val builder = StringBuilder()

        /**
         * Don't actually close anything. This is a bit of a hack, but it wasn't important to fix this is a real way.
         */
        override fun close() {
            flush()
        }

        /**
         * Flushes any buffered data. As we are working with lines of text, we add a newline to the end of the buffer
         * if there isn't one.
         */
        override fun flush() {
            if (builder.isNotEmpty()) {
                if (builder[builder.length - 1] != '\n') {
                    builder.append("\n")
                }
                out(builder.toString())
                builder.clear()
            }
        }

        /**
         * Writes the given characters to the output.
         */
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