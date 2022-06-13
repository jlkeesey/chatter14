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
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import java.io.Serializable

/**
 * Sends log event to one or more event listeners.
 */
@Suppress("unused")
@Plugin(name = "UnitTestEvent", category = "Core", elementType = "appender", printObject = true)
class UnitTestEventAppender private constructor(
    name: String?,
    filter: Filter?,
    layout: Layout<out Serializable?>,
    properties: Array<Property>,
) : AbstractAppender(name, filter, layout, true, properties) {
    class Builder<B : Builder<B>> : AbstractAppender.Builder<B>(),
                                    org.apache.logging.log4j.core.util.Builder<UnitTestEventAppender> {
        override fun build(): UnitTestEventAppender? {
            if (name == null) {
                LOGGER.error("No name provided for UnitTestEvent")
                return null
            }
            @Suppress("UsePropertyAccessSyntax") val layout =
                getOrCreateLayout() ?: PatternLayout.newBuilder().withPattern("%m").build()
            return UnitTestEventAppender(
                name = name, filter = filter, layout = layout, properties = propertyArray
            )
        }
    }

    /**
     * Actual writing occurs here.
     *
     * @param event The LogEvent.
     */
    override fun append(event: LogEvent) {
        addToList(event.message.formattedMessage)
    }

    companion object {
        private val messagesList = mutableListOf<String>()
        private val lock = Any()

        val messages: List<String> = messagesList

        private fun addToList(msg: String) {
            synchronized(lock) {
                messagesList.add(msg)
            }
        }

        fun clear() {
            synchronized(lock) {
                messagesList.clear()
            }
        }

        @Suppress("unused")
        @JvmStatic
        @PluginBuilderFactory
        fun <B : Builder<B>> newBuilder(): B {
            return Builder<B>().asBuilder()
        }
    }
}