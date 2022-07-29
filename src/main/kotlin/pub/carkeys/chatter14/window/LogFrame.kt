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

@file:OptIn(DelicateCoroutinesApi::class)

package pub.carkeys.chatter14.window

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.appender.AbstractManager
import pub.carkeys.chatter14.log4j.EventManager
import pub.carkeys.chatter14.logger
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import java.lang.Integer.max
import java.lang.Integer.min
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument

/**
 * Swing frame that displays the output log if desired.
 */
class LogFrame(private val owner: JFrame, title: String) : JFrame("$title Log") {
    private val document: StyledDocument
    private lateinit var normalStyle: Style
    private lateinit var errorStyle: Style

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        setSize(600, 400)
        locateRelativeTo(owner)

        val textPane = createTextPane()
        document = textPane.styledDocument

        // This hack allows the JTextPane to scroll but also not wrap the lines. There is a strange interaction with
        // JScrollPane that forces JTextPane to wrap lines and there is no way to stop it. Adding the extra layer
        // fixes it.
        val noWrap = JPanel(BorderLayout())
        noWrap.add(textPane)
        val scroll = JScrollPane(noWrap)

        contentPane.add(scroll)
    }

    /**
     * Creates the pane that displays the log. We also create the styles that are needed
     * to display the data.
     */
    private fun createTextPane(): JTextPane {
        val textPane = JTextPane()
        textPane.isEditable = false
        normalStyle = textPane.addStyle("normal", textPane.getStyle(StyleContext.DEFAULT_STYLE))
        StyleConstants.setFontFamily(normalStyle, "Hack")
        StyleConstants.setFontSize(normalStyle, 16)
        errorStyle = textPane.addStyle("error", normalStyle)
        StyleConstants.setForeground(errorStyle, Color.RED)

        if (AbstractManager.hasManager("JPanel")) {
            val manager = AbstractManager.getManager<EventManager, EventManager.EventData>("JPanel", null, null)
            manager.addListener(object : EventManager.Log4jEventListener {
                override fun logMessage(event: EventManager.Log4JEvent) {
                    val style = when (event.level) {
                        Level.ERROR, Level.FATAL -> errorStyle
                        else                     -> normalStyle
                    }
                    GlobalScope.launch(Dispatchers.Swing) {
                        document.insertString(document.length, event.msg, style)
                    }
                }
            })

        }

        return textPane
    }

    /**
     * Make this frame visible. When we make the frame visible we want to relocate it
     * relative to its owner.
     */
    fun makeVisible() {
        locateRelativeTo(owner)
        isVisible = true
    }

    /**
     * Attempts to move the frame next to the owner. First we try aligned with the top
     * right of the owner, then the left, and the overlapping if necessary. IF the frame
     * is too large to fit the screen we shrink it to fit.
     */
    private fun locateRelativeTo(owner: JFrame) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val ownerSize = owner.size
        val ownerLocation = owner.location

        var newX = ownerLocation.x + ownerSize.width
        var newY = ownerLocation.y
        val newWidth = max(min(width, screenSize.width), 200)
        val newHeight = max(min(height, screenSize.height), 200)

        if (newX + width > screenSize.width) {
            newX = ownerLocation.x - width
            if (newX < 0) {
                newX = screenSize.width - width - 1
            }
        }
        if (newY + height > screenSize.height) {
            newY = screenSize.height - height - 1
            if (newY < 0) {
                newY = 0
            }
        }
        setBounds(newX, newY, newWidth, newHeight)
    }

    companion object {
        val logger by logger()
    }
}
