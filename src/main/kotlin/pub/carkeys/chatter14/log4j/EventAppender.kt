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

import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.appender.AppenderLoggingException
import org.apache.logging.log4j.core.appender.WriterAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required
import org.apache.logging.log4j.core.layout.ByteBufferDestination
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.core.util.Constants
import java.io.Serializable
import java.nio.ByteBuffer

/**
 * Sends log event to one or more event listeners.
 */
@Suppress("unused")
@Plugin(name = "Event", category = "Core", elementType = "appender", printObject = true)
class EventAppender private constructor(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable?>,
    ignoreExceptions: Boolean,
    private val manager: EventManager,
    properties: Array<Property>,
) : AbstractAppender(name, filter, layout, ignoreExceptions, properties) {
    private val eventDestination = EventDestination()

    class Builder<B : Builder<B>> : AbstractAppender.Builder<B>(),
                                    org.apache.logging.log4j.core.util.Builder<EventAppender> {
        /**
         * The name that the event broadcaster (the EventManager) is registered under.
         */
        @PluginBuilderAttribute
        @Required
        private var eventTarget: String? = null

        @Suppress("unused")
        fun setEventTarget(eventTarget: String): B {
            this.eventTarget = eventTarget
            return asBuilder()
        }

        override fun build(): EventAppender? {
            if (name == null) {
                LOGGER.error("No name provided for EventAppender")
                return null
            }
            if (eventTarget == null) {
                LOGGER.error("No eventTarget provided for EventAppender")
                return null
            }
            @Suppress("UsePropertyAccessSyntax") val layout = getOrCreateLayout() ?: PatternLayout.createDefaultLayout()
            val manager = EventManager.getEventManager(
                name = eventTarget!!, configuration = configuration
            )
            return EventAppender(
                name = name,
                filter = filter,
                layout = layout,
                ignoreExceptions = true,
                manager = manager,
                properties = propertyArray
            )
        }
    }

    /**
     * Actual writing occurs here.
     *
     * @param event The LogEvent.
     */
    override fun append(event: LogEvent) {
        try {
            layout.encode(event, eventDestination)
            manager.raise(event, eventDestination.toString())
            eventDestination.clear()
        } catch (ex: AppenderLoggingException) {
            error("Unable to write to event stream " + manager.name + " for appender " + name, event, ex)
            throw ex
        }
    }

    private class EventDestination : ByteBufferDestination {
        private val byteBuffer = ByteBuffer.wrap(ByteArray(Constants.ENCODER_BYTE_BUFFER_SIZE))
        private var string: String? = null

        override fun toString(): String {
            if (string == null) {
                byteBuffer.flip()
                string = String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining())
            }
            return string!!
        }

        fun clear() {
            byteBuffer.clear()
            string = null
        }

        override fun getByteBuffer(): ByteBuffer {
            return byteBuffer
        }

        override fun drain(buf: ByteBuffer): ByteBuffer {
            throw AppenderLoggingException("This appender does not support lines longer than ${byteBuffer.capacity()}")
        }

        override fun writeBytes(buffer: ByteBuffer) {
            if (buffer.remaining() == 0) { // Because of the way Buffer works this is the current size
                return
            }
            if (buffer.remaining() > byteBuffer.remaining()) {
                LOGGER.error("This appender does not support lines longer than ${byteBuffer.capacity()}")
            } else {
                byteBuffer.put(buffer)
            }
        }

        override fun writeBytes(bytes: ByteArray, offset: Int, length: Int) {
            if (length >= byteBuffer.remaining()) {
                LOGGER.error("This appender does not support lines longer than ${byteBuffer.capacity()}")
            } else {
                byteBuffer.put(bytes, offset, length)
            }
        }
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        @PluginBuilderFactory
        fun <B : Builder<B>> newBuilder(): B {
            return Builder<B>().asBuilder()
        }
    }
}