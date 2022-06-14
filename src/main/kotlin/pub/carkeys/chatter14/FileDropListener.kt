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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.carkeys.chatter14.config.ParseOptions
import pub.carkeys.chatter14.processor.ActLogFileHandler
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

/**
 * Listens to file drop messages and invokes the log processor for each one.
 *
 * I get the felling that there is a newer, better way of doing this, but it works for this
 * simple situation so why mess with it?
 */
class FileDropListener(
    private val parseOptions: ParseOptions, private val panel: DropPanel,
) : DropTargetListener {
    /**
     * Handles the drop action on the control. For each drop item if it is a file type we collect
     * it into a list and the pass the list to log processor to handle each one.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun drop(event: DropTargetDropEvent) {
        parseOptions.files.clear()
        event.acceptDrop(DnDConstants.ACTION_COPY)
        val transferable = event.transferable
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val entries = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
            for (entry in entries) {
                if (entry is File) {
                    parseOptions.files.add(entry)
                }
            }
        }
        event.dropComplete(true)
        panel.resetImage()
        GlobalScope.launch(Dispatchers.IO) {
            ActLogFileHandler(parseOptions).process()
        }
    }

    /**
     * Handle dragging into the component. We change the background color to show that a drop is
     * possible.
     *
     * TODO: this should look for file types and only signal acceptance if they are files.
     */
    override fun dragEnter(event: DropTargetDragEvent?) {
        panel.randomImage()
    }

    /**
     * Handle dragging out of the component. We reset the background color.
     */
    override fun dragExit(event: DropTargetEvent?) {
        panel.resetImage()
    }

    override fun dragOver(event: DropTargetDragEvent?) {}
    override fun dropActionChanged(event: DropTargetDragEvent?) {}
}
