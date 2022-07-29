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

import pub.carkeys.chatter14.I18N.Companion.applicationPropertiesLoadFailed
import java.io.IOException
import java.util.*

/**
 * Contains the basic application information as generated by the build process such as
 * application name and version.
 *
 * @property name the name of this application. This is generally the application command
 *     line name.
 * @property title the application title. This is generally used as the title on the main
 *     panel of the application.
 * @property version the application version. This is the current version of the
 *     application as it is known to the build system
 */
data class ApplicationInfo(val name: String, val title: String, val version: String) {
    companion object {
        private val logger by logger()

        /**
         * Loads the application information from the generated resource property file of
         * the build. This can be invoked from a static environment.
         *
         * @return the ApplicationInfo from the properties file.
         */
        fun load(): ApplicationInfo {
            val properties = Properties()
            try {
                Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties").use {
                    properties.load(it)
                }
            } catch (e: IOException) {
                logger.error(applicationPropertiesLoadFailed.toString(), e)
            }
            val name = properties["application.name"] as? String ?: "App"
            val title = properties["application.title"] as? String ?: "App"
            val version = properties["application.version"] as? String ?: "?.?"
            return ApplicationInfo(name = name, title = title, version = version)
        }
    }
}
