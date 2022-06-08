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

package pub.carkeys.logparse.log4j

import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory
import org.apache.logging.log4j.core.layout.PatternLayout

@Plugin(name = "Event", category = "Core", elementType = "appender", printObject = true)
class EventAppender private constructor(
    name: String,
    filter: Filter?,
    layout: PatternLayout?,
    ignoreExceptions: Boolean,
    private val manager: EventManager,
    properties: Array<Property>,
) : AbstractAppender(name, filter, layout, ignoreExceptions, properties) {

    class Builder<B : Builder<B>> : AbstractAppender.Builder<B>(),
                                    org.apache.logging.log4j.core.util.Builder<EventAppender> {

        override fun build(): EventAppender? {
            if (name == null) {
                LOGGER.error("No name provided for EventAppender")
                return null
            }
            @Suppress("UsePropertyAccessSyntax") val pattern =
                (getOrCreateLayout() as? PatternLayout) ?: PatternLayout.createDefaultLayout()
            val manager = EventManager.getEventManager(name, layout = pattern, configuration = configuration)
            return EventAppender(
                name = name,
                filter = filter,
                layout = pattern,
                ignoreExceptions = true,
                manager = manager,
                properties = propertyArray
            )
        }
    }

    override fun append(event: LogEvent?) {
        if (event != null) {
            manager.raise(event)
        }
    }

    companion object {
        @JvmStatic
        @PluginBuilderFactory
        fun <B : Builder<B>> newBuilder(): B {
            return Builder<B>().asBuilder()
        }
    }
}