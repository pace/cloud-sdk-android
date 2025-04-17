package car.pace.cofu.util

import java.io.File
import java.io.InputStream
import timber.log.Timber

object FileUtils {

    fun writeFile(inputStream: InputStream, outputFile: File) {
        try {
            outputFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not write file ${outputFile.absolutePath}")
        }
    }
}
