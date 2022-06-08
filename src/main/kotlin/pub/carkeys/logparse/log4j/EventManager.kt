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

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.AbstractManager
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData
import org.apache.logging.log4j.core.appender.ManagerFactory
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.layout.PatternLayout
import java.util.*
import java.util.concurrent.TimeUnit

class EventManager(loggerContext: LoggerContext?, name: String, private val layout: PatternLayout) : AbstractManager(
    loggerContext, name
) {
    private val listeners: MutableSet<Log4jEventListener> = mutableSetOf()

    fun raise(logEvent: LogEvent) {
        if (listeners.isNotEmpty()) {
            val msg = layout.toSerializable(logEvent)
            val event = Log4JEvent(logEvent, logEvent.level, msg)
            listeners.forEach { it.logMessage(event) }
        }
    }

    fun addListener(listener: Log4jEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Log4jEventListener) {
        listeners.remove(listener)
    }

    override fun releaseSub(timeout: Long, timeUnit: TimeUnit?): Boolean {
        listeners.clear()
        return true
    }

    class Log4JEvent(source: Any, val level: Level, val msg: String) : EventObject(source)

    interface Log4jEventListener : EventListener {
        fun logMessage(event: Log4JEvent)
    }

    class EventData(val layout: PatternLayout, configuration: Configuration?) : ConfigurationFactoryData(configuration)

    companion object {
        fun hasManager(name: String): Boolean {
            return hasManager(name)
        }

        fun getEventManager(name: String, layout: PatternLayout, configuration: Configuration?): EventManager {
            return getManager(name, FACTORY, EventData(layout, configuration))
        }

        private val FACTORY: ManagerFactory<EventManager, EventData> =
            ManagerFactory<EventManager, EventData> { name, data ->
                EventManager(
                    data.loggerContext, name, data.layout
                )
            }
    }
}