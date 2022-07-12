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

package pub.carkeys.chatter14.log4j

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

/**
 * An Appendable that writes to the logging system. This can be used to write out blocks
 * of text like configuration files to the log from subsystems that don't have a logging
 * interface but do have an Appendable interface.
 *
 * @property logger the Logger to write to.
 * @property level the logging level to use.
 * @property indent the indentation to use for each line.
 */
class LoggerAppendable(
    private val logger: Logger,
    private val level: Level = Level.INFO,
    private val indent: String = "",
) : Appendable, AutoCloseable {
    private val builder = StringBuilder()
    private var atBol = true

    override fun append(csq: CharSequence?): java.lang.Appendable {
        if (csq != null) {
            append(csq, 0, csq.length)
        }
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
        if (csq != null) {
            for (i in start until end) {
                append(csq[i])
            }
        }
        return this
    }

    override fun append(c: Char): java.lang.Appendable {
        if (c == '\n') {
            flush()
        } else {
            if (atBol) {
                builder.append(indent)
                atBol = false
            }
            builder.append(c)
        }
        return this
    }

    private fun flush() {
        if (builder.isNotEmpty()) {
            logger.log(level, builder.toString())
            builder.clear()
        }
        atBol = true
    }

    override fun close() {
        flush()
    }
}