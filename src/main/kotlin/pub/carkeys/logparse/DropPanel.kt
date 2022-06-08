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

import java.awt.*
import java.awt.dnd.DropTarget
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * Display the drop panel with the controls for how to process any dropped files.
 */
class DropPanel(
    private val parseConfig: ParseConfig,
    private val parseOptions: ParseOptions,
) : JFrame("LogParse") {

    @Suppress("unused")
    private val serialVersionUID = 1L
    private var logWindow: LogFrame? = null

    private val groupLabels = parseConfig.groups.keys.toList().sorted().toTypedArray()

    private val randomImages = listOf(
        loadImage("/images/cat-shadow-ball-icon.png"),
        loadImage("/images/cat-shadow-fly-icon.png"),
        loadImage("/images/cat-shadow-lady-icon.png"),
        loadImage("/images/cat-shadow-lion-icon.png"),
        loadImage("/images/cat-shadow-whale-icon.png"),
    )
    private var imageIndex = 0
    private val normalImage = loadImage("/images/cat-shadow-icon.png")
    private var currentImage = normalImage
    private val label = createDropLabel()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        val panelSize = Dimension(350, 200)
        size = panelSize
        minimumSize = panelSize

        contentPane.add(createTopPanel(), BorderLayout.PAGE_START)
        contentPane.add(label, BorderLayout.CENTER)

        this.isVisible = true

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                logWindow?.dispose()
            }
        })

    }

    fun resetImage() {
        currentImage = normalImage
        setImage(label, currentImage)
    }

    fun randomImage() {
        currentImage = randomImages[imageIndex]
        imageIndex = (imageIndex + 1) % randomImages.size
        setImage(label, currentImage)
    }

    private fun loadImage(name: String): BufferedImage {
        val url = DropPanel::class.java.getResource(name)
        val file = File(url.toURI())
        return ImageIO.read(file)
    }

    /**
     * Creates the label which is the drop target for this application. The label is added to the
     * BorderLayout.CENTER which makes if fill all remaining space after the controls, so it is
     * suitably sized for a drop target.
     */
    private fun createDropLabel(): JLabel {
        //val label = JLabel("Drag log files here!", SwingConstants.CENTER)
        val label = JLabel("", SwingConstants.CENTER)
        setImage(label, currentImage)
        label.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                setImage(label, currentImage)
            }
        })
        label.isOpaque = true
        val dropListener = FileDropListener(parseOptions, this)
        DropTarget(label, dropListener)
        return label
    }

    private fun setImage(label: JLabel, image: BufferedImage) {
        val labelWidth = label.width.coerceAtLeast(1)
        val labelHeight = label.height.coerceAtLeast(1)

        val diffWidth = image.width - labelWidth
        val diffHeight = image.height - labelHeight

        val newWidth: Int
        val newHeight: Int

        if (diffWidth < 0 && diffHeight < 0) {
            if (diffWidth < diffHeight) {
                newWidth = -image.width
                newHeight = labelHeight
            } else {
                newWidth = labelWidth
                newHeight = -image.height
            }
        } else if (diffWidth < 0 /* && diffHeight >= 0 */) {
            newWidth = -image.width
            newHeight = labelHeight
        } else if (/* diffWidth >= 0 && */ diffHeight < 0) {
            newWidth = labelWidth
            newHeight = -image.height
        } else if (diffWidth < diffHeight) {
            newWidth = -image.width
            newHeight = labelHeight
        } else {
            newWidth = labelWidth
            newHeight = -image.height
        }
        val newImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT)
        label.icon = ImageIcon(newImage)
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
                //myLogger.setMessenger(logWindow!!)
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