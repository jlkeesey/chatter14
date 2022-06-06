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

package pub.carkeys.logparse

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.full.companionObject

@Suppress("SpellCheckingInspection")
/*
 * Code from [StackOverflow](https://stackoverflow.com/a/34462577/18295) written by
 * [Jayson Minard](https://stackoverflow.com/users/3679676/jayson-minard).
 */

/**
 * Return the logger for this class. If this class is a companion object, gets the logger for
 * the containing class.
 */
fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { logger(this.javaClass) }
}

/**
 * Isolate the acquisition of a logger.
 */
private fun <T : Any> logger(forClass: Class<T>): Logger {
    return LogManager.getLogger(unwrapCompanionClass(forClass).name)
}

/**
 * Unwrap companion class to enclosing class given a Java Class
 */
private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

/**
 * Unwrap companion class to enclosing class given a Kotlin Class
 */
//private fun <T: Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
//    return unwrapCompanionClass(ofClass.java).kotlin
//}
