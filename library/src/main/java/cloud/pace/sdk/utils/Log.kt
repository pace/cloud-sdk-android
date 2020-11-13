package cloud.pace.sdk.utils

import android.content.Context
import android.util.Log.*
import java.io.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Logger that decides to log into logcat on development environment and log files on other environments.
 */
class Log {

    private var container: String? = null
    private val explicitTag = ThreadLocal<String>()
    private var printer: PrintWriter? = null
    private var logLevel = DEBUG
    private var debug = false
    private var maxLogDays = DEFAULT_MAX_LOG_DAYS
    private var logFileFormat = DEFAULT_LOG_FILE_FORMAT

    /**
     * Returns the current tag. This may be the current thread or the root class of a stack trace.
     *
     * @return The tag or `null` if no thread is running to get a tag.
     * @see tagFromStackTrace
     */
    private val tag: String?
        get() {
            // First check if there is a valid thread to get a tag.
            val tag = explicitTag.get()
            if (tag != null) {
                explicitTag.remove()
                return tag
            }
            return null
        }

    /**
     * Returns the tag from the current stack trace. This may return a tag when [tag]
     * did return `null` but it's an expensive operation.
     *
     * @return The tag.
     * @see tag
     */
    private val tagFromStackTrace: String
        get() {
            // If there is no thread running, check the stack trace to get a tag.
            val stackTrace = Throwable().stackTrace
            return createStackElementTag(stackTrace[3])
        }

    /**
     * Extract the tag which should be used for the message from the [element]. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     */
    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        return if (tag.length > MAX_TAG_LENGTH) tag.substring(0, MAX_TAG_LENGTH) else tag
    }

    /**
     * Returns the Stacktrace of a [Throwable].
     *
     * @param t the [Throwable]
     * @return The Stacktrace.
     */
    private fun getStackTraceString(t: Throwable): String {
        // Don't replace this with Log.getStackTraceString() - it hides UnknownHostException, which is not what we want.
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        try {
            t.printStackTrace(pw)
        } catch (e: OutOfMemoryError) {
            pw.flush()
            return ""
        }

        pw.flush()
        return sw.toString()
    }

    /**
     * Return whether a message at `priority` or `tag` should be logged.
     *
     * @param priority the priority of the message
     * @return True if the message should be logged, false if not.
     */
    private fun isLoggable(priority: Int): Boolean {
        // Log everything in debug builds.
        if (debug) return true

        return when (logLevel) {
            ASSERT -> priority == ASSERT
            ERROR -> priority >= ERROR
            WARN -> priority >= WARN
            INFO -> priority >= INFO
            DEBUG -> true
            VERBOSE -> true
            else -> false
        }
    }

    /**
     * Prepare a `message` to be logged.
     *
     * @param priority the priority of the message
     * @param tag the tag
     * @param throwable the [Throwable] which could be added to the log
     * @param message the message
     * @param args the optional arguments of the message
     */
    private fun prepareLog(priority: Int, tag: String? = this.tag, throwable: Throwable?, message: String?, vararg args: Any?) {
        var newMessage = message

        if (!isLoggable(priority)) {
            return
        }

        if (newMessage != null && newMessage.isEmpty()) {
            newMessage = null
        }

        if (newMessage == null) {
            if (throwable == null) {
                return // Swallow message if it's null and there's no throwable.
            }
            newMessage = getStackTraceString(throwable)
        } else {
            if (args.isNotEmpty()) {
                newMessage = String.format(newMessage, *args)
            }

            if (throwable != null) {
                newMessage += "\n" + getStackTraceString(throwable)
            }
        }

        // Special treatment for some exceptions.
        if (throwable is UnknownHostException) {
            // Caused by missing network connection. Do not print the entire stack trace.
            newMessage = "Network request failed due to UnknownHostException"
        }

        // Filter null bytes.
        newMessage = newMessage.replace("\\x00".toRegex(), "")

        log(priority, tag ?: container, newMessage)
    }

    /**
     * Breaks up `message` into maximum-length chunks (if needed) and prints it.
     *
     * @param priority the priority of the message
     * @param tag the tag
     * @param message the message
     */
    private fun log(priority: Int, tag: String?, message: String) {
        if (message.length < MAX_LOG_LENGTH) {
            printToLogfile(priority, tag, message)
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                printToLogfile(priority, tag, part)
                i = end
            } while (i < newline)
            i++
        }
    }

    /**
     * Returns the log prefix used in PACE logging.
     *
     * @param priority the priority of the log
     * @return The prefix for the log.
     */
    private fun getLogPrefix(priority: Int): String? {
        return when (priority) {
            ASSERT -> "C"
            ERROR -> "E"
            WARN -> "W"
            INFO -> "I"
            DEBUG -> "D"
            VERBOSE -> "D"
            else -> null
        }
    }

    /**
     * Prints the `message` to the specific destinations. Logcat is only used on
     * development environment.
     *
     * @param priority the priority of the message
     * @param tag the tag
     * @param message the message
     */
    private fun printToLogfile(priority: Int, tag: String?, message: String) {
        // Log to logcat on debug builds.
        if (debug) println(priority, tag, message)

        if (printer != null) {
            val date = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
            printer!!.write("[$date] [$tag ${getLogPrefix(priority)}] $message \n")
            printer!!.flush()
        }
    }

    /**
     * Prepares the log file with the correct name and start time. If the session changed, a new
     * file will be created. The [printer] takes care of the correct destination file.
     *
     * @throws IOException Thrown if something with the file creation went wrong.
     */
    @Throws(IOException::class)
    private fun prepareLogFile(context: Context): Boolean {
        val simpleDateFormat = SimpleDateFormat(logFileFormat)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -maxLogDays)

        val files = File(context.filesDir.toString() + LOG_DIR).listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                try {
                    // Delete old files
                    if (simpleDateFormat.parse(file.name).before(calendar.time)) {
                        file.delete()
                        continue
                    }
                } catch (ignored: Exception) {
                    file.delete()
                    continue
                }
            }
        }

        // Check the log file.
        val file = File("${context.filesDir}$LOG_DIR${simpleDateFormat.format(Date())}.$LOG_FILE_EXTENSION")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        if (!file.exists()) {
            val created = file.createNewFile()
            if (!created) {
                file.delete()
                logError("Log file was not created")
                return false
            }
        }

        if (!file.canWrite()) {
            file.delete()
            logError("Cannot write into log file")
            return false
        }

        // Check the printer.
        if (printer == null || printer!!.checkError()) {
            val fos = FileOutputStream(file, true)
            val osw = OutputStreamWriter(fos, "UTF-8")
            val bw = BufferedWriter(osw)
            printer = PrintWriter(bw)
        }

        return true
    }

    /**
     * Logs an error into logcat if this is a debug build.
     *
     * @param message the message to log
     */
    private fun logError(message: String) {
        // Log to logcat on debug builds.
        if (debug) android.util.Log.e(TAG, message)
    }

    companion object {

        private val TAG = Log::class.java.canonicalName

        const val LOG_DIR = "/logs/"
        const val LOG_FILE_EXTENSION = "txt"

        private const val MAX_TAG_LENGTH = 23
        private const val MAX_LOG_LENGTH = 200
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")

        private const val DEFAULT_MAX_LOG_DAYS = 7
        private const val DEFAULT_LOG_FILE_FORMAT = "dd_MM_yyyy"

        private var INSTANCE = Log()

        fun setup(context: Context, container: String, debug: Boolean, maxLogDays: Int = DEFAULT_MAX_LOG_DAYS, logFileFormat: String = DEFAULT_LOG_FILE_FORMAT) {
            try {
                INSTANCE.container = container
                INSTANCE.debug = debug
                INSTANCE.maxLogDays = maxLogDays
                INSTANCE.logFileFormat = logFileFormat
                INSTANCE.prepareLogFile(context)
                log(VERBOSE, "Setup logging for $container")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun setupTestLogger(container: String, debug: Boolean, maxLogDays: Int = DEFAULT_MAX_LOG_DAYS, logFileFormat: String = DEFAULT_LOG_FILE_FORMAT) {
            try {
                INSTANCE.container = container
                INSTANCE.debug = debug
                INSTANCE.maxLogDays = maxLogDays
                INSTANCE.logFileFormat = logFileFormat
                log(VERBOSE, "Setup test logging for $container")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * Sets the log level.
         *
         * @param logLevel the log level
         */
        fun setLogLevel(logLevel: Int) {
            INSTANCE.logLevel = logLevel
        }

        /**
         * Log a verbose message with optional format args.
         */
        fun v(message: String?, vararg args: Any?) {
            d(message, *args)
        }

        /**
         * Log a verbose exception and a message with optional format args.
         */
        fun v(t: Throwable, message: String?, vararg args: Any?) {
            d(t, message, *args)
        }

        /**
         * Log a verbose exception.
         */
        fun v(t: Throwable) {
            d(t)
        }

        /**
         * Log a debug message with optional format args.
         */
        fun d(message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(DEBUG, null, null, message, *args)
        }

        /**
         * Log a debug exception and a message with optional format args.
         */
        fun d(t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(DEBUG, null, t, message, *args)
        }

        /**
         * Log a debug exception.
         */
        fun d(t: Throwable) {
            INSTANCE.prepareLog(DEBUG, null, t, null)
        }

        /**
         * Log an info message with optional format args.
         */
        fun i(message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(INFO, null, null, message, *args)
        }

        /**
         * Log an info exception and a message with optional format args.
         */
        fun i(t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(INFO, null, t, message, *args)
        }

        /**
         * Log an info exception.
         */
        fun i(t: Throwable) {
            INSTANCE.prepareLog(INFO, null, t, null)
        }

        /**
         * Log a warning message with optional format args.
         */
        fun w(message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(WARN, null, null, message, *args)
        }

        /**
         * Log a warning exception and a message with optional format args.
         */
        fun w(t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(WARN, null, t, message, *args)
        }

        /**
         * Log a warning exception.
         */
        fun w(t: Throwable) {
            INSTANCE.prepareLog(WARN, null, t, null)
        }

        /**
         * Log an error message with optional format args.
         */
        fun e(message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(ERROR, null, null, message, *args)
        }

        /**
         * Log an error exception and a message with optional format args.
         */
        fun e(t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(ERROR, null, t, message, *args)
        }

        /**
         * Log an error exception.
         */
        fun e(t: Throwable) {
            INSTANCE.prepareLog(ERROR, null, t, null)
        }

        /**
         * Log an critical error message with optional format args.
         */
        fun c(message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(ERROR, null, null, message, *args)
        }

        /**
         * Log an critical error exception and a message with optional format args.
         */
        fun c(t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(ERROR, null, t, message, *args)
        }

        /**
         * Log an critical error exception.
         */
        fun c(t: Throwable) {
            INSTANCE.prepareLog(ERROR, null, t, null)
        }

        /**
         * Log at [priority] a message with optional format args.
         */
        fun log(priority: Int, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(priority, null, null, message, *args)
        }

        /**
         * Log at [priority] and [tag] a message with optional format args.
         */
        fun log(priority: Int, tag: String, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(priority, tag, null, message, *args)
        }

        /**
         * Log at [priority] an exception and a message with optional format args.
         */
        fun log(priority: Int, t: Throwable, message: String?, vararg args: Any?) {
            INSTANCE.prepareLog(priority, null, t, message, *args)
        }

        /**
         * Log at [priority] an exception.
         */
        fun log(priority: Int, t: Throwable) {
            INSTANCE.prepareLog(priority, null, t, null)
        }

        /**
         * Replace Log class with mocked version - important for Unit Tests environment.
         *
         * @param mocked mocked Log class.
         */
        fun injectMockedInstance(mocked: Log) {
            INSTANCE = mocked
        }

        /**
         * Zips the files in [files] list into [zipFile].
         *
         * @param zipFile Output zip file
         * @param files List with files to zip
         */
        fun createZipFile(zipFile: File, files: List<File>?) {
            if (files.isNullOrEmpty()) return
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
                files.forEach {
                    FileInputStream(it).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(it.toString().substring(it.toString().lastIndexOf("/") + 1))
                            out.putNextEntry(entry)
                            origin.copyTo(out, 1024)
                        }
                    }
                }
            }
        }

        /**
         * Deletes all zip files in [directory].
         *
         * @param directory Folder in which all zip files should be deleted
         */
        fun cleanUpZipFiles(directory: String) {
            File(directory).listFiles().forEach {
                if (it.extension == "zip") it.delete()
            }
        }
    }
}
