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

interface Messenger {
    fun message(msg: String)
    fun error(msg: String)
}

class LogFrame(private val owner: JFrame) : JFrame("LogParse Log"), Messenger {
    private val document: StyledDocument
    private lateinit var normalStyle: Style
    private lateinit var errorStyle: Style

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        setSize(400, 400)
        locateRelativeTo(owner)

        val textPane = createTextPane()
        document = textPane.styledDocument

        document.insertString(0, "This is a string 000\n", errorStyle)
        document.insertString(document.length, "123456789012345678901234567890\n", normalStyle)

        val noWrap = JPanel(BorderLayout())
        noWrap.add(textPane)
        val scroll = JScrollPane(noWrap)

        contentPane.add(scroll, BorderLayout.CENTER)
    }

    private fun createTextPane(): JTextPane {
        val textPane = JTextPane()
        textPane.isEditable = false
        normalStyle = textPane.addStyle("normal", textPane.getStyle(StyleContext.DEFAULT_STYLE))
        StyleConstants.setFontFamily(normalStyle, "Hack")
        StyleConstants.setFontSize(normalStyle, 16)
        errorStyle = textPane.addStyle("error", normalStyle)
        StyleConstants.setForeground(errorStyle, Color.RED)

        return textPane
    }

    override fun message(msg: String) {
        document.insertString(document.length, msg, normalStyle)
    }

    override fun error(msg: String) {
        document.insertString(document.length, msg, errorStyle)
    }

    fun makeVisible() {
        locateRelativeTo(owner)
        isVisible = true
    }

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
}
