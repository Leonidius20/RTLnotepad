package ua.leonidius.rtlnotepad.utils

import android.os.AsyncTask
import android.util.Log

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Used for asynchronous writing of a string into a file. The result is returned via a callback
 * (false if failed to write, true otherwise).
 */
class WriteTask(private val file: File, private val text: String, private val encoding: String, private val callback: (Boolean) -> Unit) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg voids: Void): Boolean {
        return try {
            val bw = BufferedWriter(OutputStreamWriter(FileOutputStream(file), encoding))
            bw.write(text)
            bw.close()
            true
        } catch (e: Exception) {
            Log.e("WriteTask RTLnotepad", e.message!!)
            false
        }

    }

    override fun onPostExecute(success: Boolean) {
        callback.invoke(success)
    }

}