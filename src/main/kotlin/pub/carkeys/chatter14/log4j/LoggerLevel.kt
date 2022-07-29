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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.LoggerConfig

/**
 * Sets the logging level for the given logger and all of its children. The default is to
 * set the level for the root logger.
 */
fun loggerLevel(level: Level, loggerName: String = LogManager.ROOT_LOGGER_NAME) {
    val context: LoggerContext = LogManager.getContext(false) as LoggerContext
    val configuration: Configuration = context.configuration
    val loggerConfig: LoggerConfig = configuration.getLoggerConfig(loggerName)
    loggerConfig.level = level
    context.updateLoggers() // Update children
}

/*
 * Code for loggerLevel() from [StackOverflow](https://stackoverflow.com/a/23434603/18295)
 */