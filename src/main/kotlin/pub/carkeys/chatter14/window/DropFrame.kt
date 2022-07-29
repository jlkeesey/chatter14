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
import pub.carkeys.chatter14.I18N
import pub.carkeys.chatter14.config.ParseConfiguration
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.processor.ActLogFileHandler
import java.awt.*
import java.awt.dnd.DropTarget
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * Display the drop panel with the controls for how to process any dropped files.
 */
class DropFrame(
    private val config: ParseConfiguration,
    private val options: ParseOptions,
    private val info: ApplicationInfo,
    private val fileHandler: ActLogFileHandler,
) : JFrame(info.title) {

    @Suppress("unused")
    private val serialVersionUID = 1L
    private var logWindow: LogFrame? = null

    private val groupLabels = config.groups.values.map { it.label }.toList().sorted().toTypedArray()

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

    private val dropColor = Color(0.22353f, 0.67451f, 0.45098f)

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
        label.background = Color.WHITE
    }

    fun randomImage() {
        currentImage = randomImages[imageIndex]
        imageIndex = (imageIndex + 1) % randomImages.size
        setImage(label, currentImage)
        label.background = dropColor
    }

    private fun loadImage(name: String): BufferedImage {
        val stream = DropFrame::class.java.getResourceAsStream(name) ?: throw MissingResourceException(
            "Image $name was not found", DropFrame::class.java.name, name
        )
        return ImageIO.read(stream)
    }

    /**
     * Creates the label which is the drop target for this application. The label is
     * added to the BorderLayout.CENTER which makes if fill all remaining space after the
     * controls, so it is suitably sized for a drop target.
     */
    private fun createDropLabel(): JLabel {
        val label = JLabel("", SwingConstants.CENTER)
        setImage(label, currentImage)
        label.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                setImage(label, currentImage)
            }
        })
        label.isOpaque = true
        val dropListener = FileDropListener(options = options, panel = this, fileHandler = fileHandler)
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
     * Creates the top panel which contains all the controls for affecting the log
     * processor.
     */
    private fun createTopPanel(): JPanel {
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
        val control = JButton(I18N.labelLog.toString())
        control.setSize(20, control.height)
        control.addActionListener {
            if (logWindow == null) {
                logWindow = LogFrame(owner = this, title = info.title)
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
        control.selectedItem = options.group.label
        control.addActionListener { action ->
            val cb = action.source as JComboBox<*>
            val item = cb.selectedItem as String
            options.group = config.groups[item]!!
        }
        return control
    }

    /**
     * Creates the dry run checkbox.
     */
    private fun createDryRunControl(): JCheckBox {
        val checkBox = JCheckBox(I18N.labelDryRun.toString(), options.dryRun)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            options.dryRun = cb.isSelected
        }
        return checkBox
    }

    /**
     * Creates the process emotes checkbox.
     */
    private fun createEmotesControl(): JCheckBox {
        val checkBox = JCheckBox(I18N.labelEmotes.toString(), options.includeEmotes)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            options.includeEmotes = cb.isSelected
        }
        return checkBox
    }

    /**
     * Creates the force file replacement checkbox.
     */
    private fun createForceControl(): JCheckBox {
        val checkBox = JCheckBox(I18N.labelReplaceFiles.toString(), options.forceReplace)
        checkBox.addActionListener { action ->
            val cb = action.source as JCheckBox
            options.forceReplace = cb.isSelected
        }
        return checkBox
    }
}