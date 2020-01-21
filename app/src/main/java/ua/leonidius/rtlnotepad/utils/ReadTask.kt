package ua.leonidius.rtlnotepad.utils

import android.net.Uri
import android.os.AsyncTask
import ua.leonidius.rtlnotepad.MainActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Used for asynchronous reading of a file into a string. The string is returned via
 * a callback. If failed to read the file, the resulting string would be null.
 */
/*class ReadTask(private val file: File, private val encoding: String, private val callback: (String) -> Unit) : AsyncTask<Void, Void, String>() {

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

}*/

class ReadTask(private val uri: Uri, private val encoding: String, private val callback: (String?) -> Unit) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void?): String? {
        try {
            // TODO: replace with WeakReference or something
            val contentResolver = MainActivity.instance.applicationContext.contentResolver
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        callback(result)
    }

}