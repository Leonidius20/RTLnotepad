package ua.leonidius.rtlnotepad.utils

import android.os.AsyncTask
import android.util.Log

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Used for asynchronous reading of a file into a string. The string is returned via
 * a callback. If failed to read the file, the resulting string would be null.
 */
class ReadTask(private val file: File, private val encoding: String, private val callback: (String) -> Unit) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        try {
            val br = BufferedReader(InputStreamReader(FileInputStream(file), encoding))
            val sb = StringBuilder()
            while (!isCancelled) {
                val readLine = br.readLine() ?: return sb.toString()
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(readLine)
            }
        } catch (e: Exception) {
            Log.e("ReadTask RTLnotepad", e.message)
            return null
        }

        return null
    }

    override fun onPostExecute(result: String) {
        callback.invoke(result)
    }

}