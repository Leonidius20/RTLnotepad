package ua.leonidius.rtlnotepad.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Used for asynchronous writing of a string into a file. The result is returned via a callback
 * (false if failed to write, true otherwise).
 */
class WriteTask(private val contentResolver : ContentResolver,
                private val uri: Uri, private val text : String,
                private val encoding: String,
                private val callback: (Boolean) -> Unit) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void?): Boolean {
        return contentResolver.openFileDescriptor(uri, "w")?.use {
            try {
                BufferedWriter(OutputStreamWriter(FileOutputStream(it.fileDescriptor), encoding)).use { writer ->
                    writer.write(text)
                }
                return@use true
            } catch (e: Exception) {
                e.printStackTrace()
                return@use false
            }
        }!!
    }

    override fun onPostExecute(result: Boolean) {
        callback(result)
    }

}