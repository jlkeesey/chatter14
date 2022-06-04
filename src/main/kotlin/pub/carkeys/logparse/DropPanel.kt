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
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.dnd.DropTarget
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Display the drop panel with the controls for how to process any dropped files.
 */
class DropPanel(
    private val logger: Logger,
    private val parseConfig: ParseConfig,
    private val parseOptions: ParseOptions = ParseOptions(),
) : JFrame("LogParse") {

    @Suppress("unused")
    private val serialVersionUID = 1L
    private var logWindow: LogFrame? = null

    private val groupLabels = parseConfig.groups.keys.toList().sorted().toTypedArray()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        val panelSize = Dimension(350, 200)
        size = panelSize
        minimumSize = panelSize

        contentPane.add(createTopPanel(), BorderLayout.PAGE_START)
        contentPane.add(createDropLabel(), BorderLayout.CENTER)

        this.isVisible = true
    }

    /**
     * Creates the label which is the drop target for this application. The label is added to the BorderLayout.CENTER
     * which makes if fill all remaining space after the controls, so it is suitably sized for a drop target.
     */
    private fun createDropLabel(): JLabel {
        val label = JLabel("Drag log files here!", SwingConstants.CENTER)
        label.isOpaque = true
        val dropListener = FileDropListener(parseOptions, label, logger)
        DropTarget(label, dropListener)
        return label
    }

    /**
     * Creates the top panel which contains all the controls for affecting the LogParse process.
     */
    private fun createTopPanel(): JPanel {
        // val panel = JPanel(FlowLayout())
        val topPanel = JPanel(FlowLayout())
        val panel = JPanel(GridLayout(0, 2, 10, 0))
        panel.border = EmptyBorder(0, 10, 0, 0)
        panel.add(createParticipantsControl())
        panel.add(createForceControl())
        panel.add(createEmotesControl())
        panel.add(createDryRunControl())
        topPanel.add(panel)
        topPanel.add(createShowLogControl())
        return topPanel
    }

    /**
     * Creates the show log button.
     */
    private fun createShowLogControl(): JButton {
        val control = JButton("Log")
        control.setSize(20, control.height)
        control.addActionListener {
            if (logWindow == null) {
                logWindow = LogFrame(this)
                logger.setMessenger(logWindow!!)
            }
            logWindow?.makeVisible()
        }
        return control
    }

    /**
     * Creates the participants combobox.
     */
    private fun createParticipantsControl(): JComboBox<String> {
        val control = JComboBox(groupLabels)
        control.selectedItem = parseOptions.group.label
        control.addActionListener { action ->
            val cb = action.source as JComboBox<*>
            val item = cb.selectedItem as String
            parseOptions.group = parseConfig.groups[item]!!
        }
        return control
    }

    /**
     * Creates the dry run checkbox.
     */
    private fun createDryRunControl(): JCheckBox {
        val checkBox = JCheckBox("Dry run", parseOptions.dryRun)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.dryRun = cb.isSelected
        }
        return checkBox
    }

    /**
     * Creates the process emotes checkbox.
     */
    private fun createEmotesControl(): JCheckBox {
        val checkBox = JCheckBox("Emotes", parseOptions.includeEmotes)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.includeEmotes = cb.isSelected
        }
        return checkBox
    }

    /**
     * Creates the force file replacement checkbox.
     */
    private fun createForceControl(): JCheckBox {
        val checkBox = JCheckBox("Replace files", parseOptions.forceReplace)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            parseOptions.forceReplace = cb.isSelected
        }
        return checkBox
    }

}