package ua.leonidius.rtlnotepad.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Used for asynchronous reading of a file into a string. The string is returned via
 * a callback. If failed to read the file, the resulting string would be null.
 */
class ReadTask(private val contentResolver : ContentResolver,
               private val uri: Uri, private val encoding: String,
               private val callback: (String?) -> Unit) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void?): String? {
        try {
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, encoding)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null && !isCancelled) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            return if (isCancelled) null else stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        callback(result)
    }

}