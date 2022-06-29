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

package pub.carkeys.chatter14.window

import pub.carkeys.chatter14.ApplicationInfo
import pub.carkeys.chatter14.config.ParseConfiguration
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.logger
import pub.carkeys.chatter14.processor.ActLogFileHandler
import java.awt.Font
import java.awt.FontFormatException
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

class WindowManager {
    /**
     * Starts the application in windowed, drag-and-drop mode.
     */
    fun start(
        config: ParseConfiguration,
        options: ParseOptions,
        info: ApplicationInfo,
        fileHandler: ActLogFileHandler,
    ) {
        registerFonts()

        // We wait for the panel to close before returning so that we keep the log sequence correct.
        // Without the latch, the main thread would exit as soon as the panel was started.
        val panelClosedLatch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            val panel = DropFrame(config = config, options = options, info = info, fileHandler = fileHandler)
            panel.addWindowListener(object : WindowAdapter() {
                override fun windowClosed(e: WindowEvent?) {
                    super.windowClosed(e)
                    panelClosedLatch.countDown()
                }
            })
        }
        panelClosedLatch.await()
    }

    /**
     * Register needed fonts with the Swing environment. All fonts in the resources/fonts
     * directory will be registered. Current we only load TrueType files.
     */
    private fun registerFonts() {
        val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val trueTypeFile = Regex("^.*\\.ttf$")
        this.javaClass.getResourceAsStream("/fonts").use { directory ->
            BufferedReader(InputStreamReader(directory!!)).use { reader ->
                reader.lineSequence().filter { line ->
                    trueTypeFile.matches(line)
                }.forEach {
                    registerFont(graphicsEnvironment, "/fonts/$it")
                }
            }
        }
    }

    /**
     * Register a single font from a resource path.
     *
     * @param graphicsEnvironment the environment to load the font into.
     * @param filename the fully qualified path name in the resource form.
     * @param format the font format.
     */
    private fun registerFont(
        graphicsEnvironment: GraphicsEnvironment,
        filename: String,
        format: Int = Font.TRUETYPE_FONT,
    ) {
        logger.info("Registering font $filename")
        this.javaClass.getResourceAsStream(filename).use { input ->
            try {
                val customFont = Font.createFont(format, input)
                graphicsEnvironment.registerFont(customFont)
            } catch (e: IOException) {
                logger.error("IO error reading font '${filename}'", e)
            } catch (e: FontFormatException) {
                logger.error("Font format error reading font '${filename}'", e)
            }
        }
    }

    companion object {
        private val logger by logger()
    }
}