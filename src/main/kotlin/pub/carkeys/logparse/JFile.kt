package pub.carkeys.logparse

import java.io.File
import java.nio.file.Path

class JFile(path: File) {
    private val file: File = path.canonicalFile

    init {
        if (!file.exists()) {
            throw IllegalArgumentException("'${path.name}' does not exist")
        }
        if (!file.isFile) {
            throw IllegalArgumentException("'${path.name}' is not a file")
        }
    }

    fun forEach(action: (Int, String) -> Unit) {
        var lineNumber = 1
        file.forEachLine { line ->
            action(lineNumber, line)
            lineNumber++
        }
    }
}