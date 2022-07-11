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

import java.util.*

/**
 * Defines the internationalization code specifically the message loading code.
 */
class I18N private constructor() {
    /**
     * Defines a translatable string.
     *
     * @property key the key used to lookup the message in the locale specific resource bundle.
     * @property fallback the fallback string when the key is not found in the bundle.
     * @property description the description of the string to help the translators translate the
     *     string. This will be written to the default message bundle as a comment.
     */
    class Message(private val key: String, private val fallback: String, private val description: String) {
        override fun toString(): String {
            return bundle.getString(key) ?: fallback
        }

        /**
         * Writes this message to the given Appendable. This is only used to create the default
         * message file so that it matches the code.
         */
        fun write(appendable: Appendable) {
            description.split("\n").forEach { line -> appendable.append("# ").append(line).append('\n') }
            appendable.append(key).append("=").append(fallback)
        }
    }

    companion object {
        /**
         * The appropriate message bundle based on the current locale.
         *
         * NOTE: In the current system, this is only called once at startup time so changing the
         * locale after the application starts will have no effect on this application.
         */
        private val bundle = ResourceBundle.getBundle(
            "pub.carkeys.chatter14.i18n.messages", Locale.getDefault(), Companion::class.java.classLoader
        )

        val applicationName = Message(
            "application_name", "Chatter 14", """
                The name of this application. Not sure if this should be translated but here it 
                is in case it should be.
            """.trimIndent()
        )

        val defaultDataCenterNotFound = Message(
            "default_datacenter_not_found", "Default data center Crystal not defined.", """
                Displayed as an error message to indicate that the data center named
                Crystal is not defined in the loaded data center list.
            """.trimIndent()
        )

        val dataCenterDefinitionsNotLoaded = Message(
            "datacenter_definitions_not_loaded", "Data center definitions could not be loaded.", """
                Displayed as an error message to indicate that the builtin data center
                definition file could not be read.
            """.trimIndent()
        )

        /**
         * All of the translatable strings of the system. All new messages should be added at the
         * end of this list to simplify translation. DO NOT SORT this list.
         */
        private val messages = listOf(applicationName, defaultDataCenterNotFound, dataCenterDefinitionsNotLoaded)

        /**
         * Writes out all of the messages, in order, to the given Appendable. This is only used to
         * create the default message file so that it matches the code.
         */
        fun write(appendable: Appendable) {
            messages.forEach { message ->
                message.write(appendable)
                appendable.append("\n")
            }
        }
    }
}