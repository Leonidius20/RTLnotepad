package ru.elifantiev.android.roboerrorreporter

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

internal class ExceptionHandler private constructor(context: Context, chained: Boolean) : Thread.UncaughtExceptionHandler {

    private val formatter = SimpleDateFormat("dd.MM.yy HH:mm", Locale.US)
    private val fileFormatter = SimpleDateFormat("dd-MM-yy", Locale.US)
    private var versionName = "0"
    private var versionCode = 0
    private val stacktraceDir: String
    private val previousHandler: Thread.UncaughtExceptionHandler?

    init {

        val mPackManager = context.packageManager
        val mPackInfo: PackageInfo
        try {
            mPackInfo = mPackManager.getPackageInfo(context.packageName, 0)
            versionName = mPackInfo.versionName
            versionCode = mPackInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // ignore
        }

        if (chained)
            previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        else
            previousHandler = null
        stacktraceDir = String.format("/Android/data/%s/files/", context.packageName)
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val state = Environment.getExternalStorageState()
        val dumpDate = Date(System.currentTimeMillis())
        if (Environment.MEDIA_MOUNTED == state) {

            val reportBuilder = StringBuilder()
            reportBuilder
                    .append("\n\n\n")
                    .append(formatter.format(dumpDate)).append("\n")
                    .append(String.format("Version: %s (%d)\n", versionName, versionCode))
                    .append(thread.toString()).append("\n")
            processThrowable(exception, reportBuilder)

            val sd = Environment.getExternalStorageDirectory()
            val stacktrace = File(
                    sd.path + stacktraceDir,
                    String.format(
                            "stacktrace-%s.txt",
                            fileFormatter.format(dumpDate)))
            val dumpdir = stacktrace.parentFile
            val dirReady = dumpdir!!.isDirectory || dumpdir.mkdirs()
            if (dirReady) {
                var writer: FileWriter? = null
                try {
                    writer = FileWriter(stacktrace, true)
                    writer.write(reportBuilder.toString())
                } catch (e: IOException) {
                    // ignore
                } finally {
                    try {
                        writer?.close()
                    } catch (e: IOException) {
                        // ignore
                    }

                }
            }
        }
        previousHandler?.uncaughtException(thread, exception)
    }

    private fun processThrowable(exception: Throwable?, builder: StringBuilder) {
        if (exception == null)
            return
        val stackTraceElements = exception.stackTrace
        builder
                .append("Exception: ").append(exception.javaClass.getName()).append("\n")
                .append("Message: ").append(exception.message).append("\nStacktrace:\n")
        for (element in stackTraceElements) {
            builder.append("\t").append(element.toString()).append("\n")
        }
        processThrowable(exception.cause, builder)
    }

    companion object {

        fun inContext(context: Context): ExceptionHandler {
            return ExceptionHandler(context, true)
        }

        fun reportOnlyHandler(context: Context): ExceptionHandler {
            return ExceptionHandler(context, false)
        }
    }
}
