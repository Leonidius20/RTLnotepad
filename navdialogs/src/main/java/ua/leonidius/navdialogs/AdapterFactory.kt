package ua.leonidius.navdialogs

import android.content.Context
import android.widget.SimpleAdapter

import java.io.File
import java.util.*
import kotlin.Comparator

internal object AdapterFactory {

    fun getFileAdapter(context: Context, directory: File): SimpleAdapter {
        val unsortedFiles = directory.listFiles()

        val filesSet = sortedSetOf<File>(Comparator { file1, file2 ->
            if (file1.isDirectory && !file2.isDirectory) -1
            else if (!file1.isDirectory && file2.isDirectory) 1
            else file1.name.toLowerCase().compareTo(file2.name.toLowerCase())
        }, *unsortedFiles!!)

        val data = ArrayList<Map<String, Any>>()
        var m: MutableMap<String, Any>
        for (file in filesSet) {
            m = HashMap()
            m["name"] = file.name
            if (file.isDirectory)
                m["icon"] = R.drawable.folder
            else
                m["icon"] = R.drawable.file
            data.add(m)
        }
        val from = arrayOf("name", "icon")
        val to = intArrayOf(R.id.listItemText, R.id.listItemIcon)
        return SimpleAdapter(context, data, R.layout.navdialogs_files_list_item, from, to)
    }

}