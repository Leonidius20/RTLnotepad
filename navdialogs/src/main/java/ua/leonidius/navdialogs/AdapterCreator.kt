package ua.leonidius.navdialogs

import android.content.Context
import android.widget.SimpleAdapter

import java.io.File
import java.util.*
import kotlin.Comparator

fun getFileAdapter(context: Context, directory: File): SimpleAdapter {
    val sortedFiles = sortedSetOf<File>(Comparator { file1, file2 ->
        if (file1.isDirectory && !file2.isDirectory) -1
        else if (!file1.isDirectory && file2.isDirectory) 1
        else file1.name.toLowerCase().compareTo(file2.name.toLowerCase())
    }, *directory.listFiles()!!)

    val data = ArrayList<Map<String, Any>>()
    var map: MutableMap<String, Any>
    for (file in sortedFiles) {
        map = HashMap()
        map["name"] = file.name
        if (file.isDirectory)
            map["icon"] = R.drawable.folder
        else
            map["icon"] = R.drawable.file
        data.add(map)
    }
    val from = arrayOf("name", "icon")
    val to = intArrayOf(R.id.listItemText, R.id.listItemIcon)
    return SimpleAdapter(context, data, R.layout.navdialogs_files_list_item, from, to)
}

