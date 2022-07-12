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

package pub.carkeys.chatter14.config

import cc.ekblad.toml.TomlMapper
import cc.ekblad.toml.decodeWithDefaults
import cc.ekblad.toml.encodeTo
import pub.carkeys.chatter14.I18N
import pub.carkeys.chatter14.logger
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Helper object for reading and writing Toml configuration files.
 *
 * @property filenames the list of names to search for for a configuration.
 * @property mapper the field mappings.
 * @property defaultConfigResourceName if present the resource name to load if none of the
 *     filenames are present.
 */
class ConfigurationIO(
    private val filenames: List<String>,
    val mapper: TomlMapper,
    private val defaultConfigResourceName: String? = null,
) {
    /**
     * Writes the current state of the given object to the given Appendable.
     */
    fun <T : Any> write(appendable: Appendable, value: T) {
        mapper.encodeTo(appendable, value)
    }

    /**
     * Tries to read the configuration from each of the files and default resource. If none of
     * them can be read, the defaultValue is returned.
     */
    inline fun <reified T : Any> read(defaultValue: T): T {
        return mapper.decodeWithDefaults(defaultValue, readConfigFile())
    }

    /**
     * Reads the given configuration file and returns the contents as a string. Null is returned
     * if the file cannot be read.
     */
    fun readConfigFile(): String {
        try {
            val stream = findConfigurationFile(filenames) ?: return ""
            val bufferedReader: BufferedReader = stream.bufferedReader()
            return bufferedReader.use { it.readText() }
        } catch (e: IOException) {
            logger.error(I18N.logConfigurationReadError.format(filenames.joinToString(", ")), e)
            return ""
        }
    }

    /**
     * Returns a File object for the first configuration file found in the given list. Each file
     * is tested in the current directory and then user's home directory. If no configuration
     * file is found, then we attempt to open the defaultConfigResourceName as a resource. If
     * that is not found, null is returned.
     */
    private fun findConfigurationFile(filenames: List<String>): InputStream? {
        val home = System.getProperty("user.home", ".")
        val homeDir = File(home)
        var file: File
        filenames.forEach { filename ->
            file = File(filename)
            if (file.exists()) {
                logger.info(I18N.logConfigurationFileExists.format())
                return file.inputStream()
            }
            file = File(homeDir, filename)
            if (file.exists()) {
                logger.info(I18N.logConfigurationFileExists.format())
                return file.inputStream()
            }
        }
        if (defaultConfigResourceName == null) {
            return null
        }
        logger.info(I18N.logConfigurationUsingDefaults.format(defaultConfigResourceName))
        return this.javaClass.getResourceAsStream("/$defaultConfigResourceName")
    }

    companion object {
        private val logger by logger()
    }
}