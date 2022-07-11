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

package pub.carkeys.chatter14.ffxiv

import cc.ekblad.toml.tomlMapper
import pub.carkeys.chatter14.I18N
import pub.carkeys.chatter14.config.ChatterConfigurationException
import pub.carkeys.chatter14.config.ConfigurationIO

/**
 * The complete set of data centers for Final Fantasy 14. The data is read from a
 * configuration file so that it can be updated if there are any changes between releases of
 * this application.
 *
 * @property dataCenters the list of data centers.
 */
data class Universe(val dataCenters: List<DataCenter> = listOf()) {
    /**
     * A data center.
     *
     * @property name the name of the data center.
     * @property servers the set of server names belonging to this data center.
     */
    data class DataCenter(val name: String, val servers: Set<String>)

    /**
     * Returns the data center with the given name. Names are compared case insensitively.
     * Returns null if there is no data center with the given name.
     */
    operator fun get(name: String): DataCenter? {
        // This is inefficient as it scans the list every time. However, in this app the list is
        // only scanned once so it is not a problem. If that changes, a map should be created from
        // the list and that used to find the data center.
        return dataCenters.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Writes the current state of the data center configuration to the given Appendable.
     */
    fun write(appendable: Appendable) {
        configurationIO.write(appendable, this)
    }

    companion object {
        private val INSTANCE: Universe by lazy { read() }

        /**
         * The default data center to use if no other center is specified. Currently set to Crystal
         * because that's my data center.
         */
        val DEFAULT: DataCenter by lazy {
            INSTANCE["Crystal"] ?: throw ChatterConfigurationException(I18N.defaultDataCenterNotFound)
        }

        val dataCenterNames: List<String>
            get() = INSTANCE.dataCenters.map { it.name }

        /**
         * Returns the data center with the given name.
         */
        operator fun get(name: String): DataCenter? {
            return INSTANCE[name]
        }

        /**
         * The files to read for new data center specifications.
         */
        private val dataCenterFiles = listOf(".chatter14.datacenters.toml", "chatter14.datacenters.toml")

        /**
         * Default data center configuration resource name. This is loaded if no other configuration
         * is present.
         */
        private const val defaultDataCenterConfiguration = "datacenters.toml"

        private val defaultValues = Universe()

        /**
         * Used by the Toml parser to map configuration file field names to code names.
         */
        private val mapper = tomlMapper {
            mapping<Universe>("datacenter" to "dataCenters")
        }

        /**
         * The read/writer helper for this type.
         */
        private val configurationIO = ConfigurationIO(
            filenames = dataCenterFiles, defaultConfigResourceName = defaultDataCenterConfiguration, mapper = mapper
        )

        /**
         * Loads the universe configuration.
         */
        private fun read(): Universe {
            val result = configurationIO.read(defaultValues)
            if (result.dataCenters.isEmpty()) {
                throw ChatterConfigurationException(I18N.dataCenterDefinitionsNotLoaded)
            }
            return result
        }

        /**
         * Loads the universe configuration.
         */
        fun write(appendable: Appendable) {
            INSTANCE.write(appendable)
        }
    }
}
