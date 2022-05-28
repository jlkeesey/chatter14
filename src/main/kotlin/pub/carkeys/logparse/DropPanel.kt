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
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.dnd.DropTarget
import javax.swing.*
import javax.swing.border.EmptyBorder


private const val AELYM_AND_TIFAA = "Aelym and Tifaa"
private const val AND_FIORA = "... and Fiora"
private const val EVERYONE = "Everyone"

class DropPanel : JFrame("LogParse") {

    @Suppress("unused")
    private val serialVersionUID = 1L
    private val parseOptions = ParseOptions()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(300, 150)

        contentPane.add(createTopPanel(), BorderLayout.PAGE_START)
        contentPane.add(createDropLabel(), BorderLayout.CENTER)

        this.isVisible = true
    }

    private fun createDropLabel(): JLabel {
        val label = JLabel("Drag something here!", SwingConstants.CENTER)
        label.isOpaque = true
        val dropListener = FileDropListener(parseOptions, label)
        DropTarget(label, dropListener)
        return label
    }

    private fun createTopPanel(): JPanel {
        // val panel = JPanel(FlowLayout())
        val panel = JPanel(GridLayout(0, 2, 10, 0))
        panel.border = EmptyBorder(0, 10, 0, 0);
        panel.add(createParticipantsControl())
        panel.add(createForceControl())
        panel.add(createEmotesControl())
        panel.add(createDryRunControl())
        return panel
    }

    private fun createParticipantsControl(): JComboBox<String> {
        val items = arrayOf(AELYM_AND_TIFAA, AND_FIORA, EVERYONE)
        val control = JComboBox(items)
        control.addActionListener { action ->
            val cb = action.source as JComboBox<*>
            val item = cb.selectedItem as String
            parseOptions.participantType = when (item) {
                AELYM_AND_TIFAA -> ParticipantType.PRIMARY
                AND_FIORA       -> ParticipantType.SECONDARY
                else            -> ParticipantType.ALL
            }
        }
        return control
    }

    private fun createDryRunControl(): JCheckBox {
        val checkBox = JCheckBox("Dry run", parseOptions.dryRun)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.dryRun = cb.isSelected
        }
        return checkBox
    }

    private fun createEmotesControl(): JCheckBox {
        val checkBox = JCheckBox("Emotes", parseOptions.shouldProcessEmotes)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.shouldProcessEmotes = cb.isSelected
        }
        return checkBox
    }

    private fun createForceControl(): JCheckBox {
        val checkBox = JCheckBox("Replace files", parseOptions.forceReplace)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.forceReplace = cb.isSelected
        }
        return checkBox
    }

}