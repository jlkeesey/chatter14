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

import java.io.IOException
import java.util.*

data class ApplicationInfo(val name: String, val version: String) {
    companion object {
        private val logger by logger()

        fun loadInfo(): ApplicationInfo {
            val properties = Properties()
            try {
                Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties").use {
                    properties.load(it)
                }
            } catch (e: IOException) {
                logger.error("Failed to load application.properties", e)
            }
            val name = properties["application.name"] as? String ?: "App"
            val version = properties["application.version"] as? String ?: "1.4"
            return ApplicationInfo(name = name, version = version)
        }
    }
}
