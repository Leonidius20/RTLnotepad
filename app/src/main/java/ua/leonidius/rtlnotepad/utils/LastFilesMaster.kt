package ua.leonidius.rtlnotepad.utils

import android.app.Activity
import android.widget.SimpleAdapter
import ua.leonidius.rtlnotepad.MainActivity
import ua.leonidius.rtlnotepad.R
import java.io.File
import java.io.IOException
import java.util.*

/**
 * This class manages the list of last opened files.
 */

object LastFilesMaster {
    private lateinit var slots: LinkedList<File?>// stores files' paths
    private val slot_names = arrayOf("slot1", "slot2", "slot3", "slot4", "slot5")
    private const val EMPTY = "empty"
    private const val SLOTS_SIZE = 5

    fun add(file: File) {
        // Checking if file is already on the list and placing it to the top in this case
        for (fileInSlot in slots) {
            if (fileInSlot === file) {
                slots.remove(fileInSlot)
                slots.addFirst(file)
                return
            }
        }

        if (slots.size == SLOTS_SIZE) slots.removeLast()
        slots.addFirst(file)
    }

    // Retrieves recent files saved in Preferences. Called in main activity's onCreate()
    fun initSlots(activity: MainActivity) {
        slots = LinkedList()
        var path: String?
        var file: File
        for (i in 0 until SLOTS_SIZE) {
            path = activity.pref.getString(slot_names[i], EMPTY)
            if (path != EMPTY) {
                file = File(path!!)
                if (file.exists())
                    slots.add(file)
                else
                    slots.add(null)
            } else
                slots.add(null)
        }
    }

    // Saves recent files to the preferences. Called in main activity's onSaveInstanceState()
    fun saveSlots(activity: MainActivity) {
        val edit = activity.pref.edit()
        for (i in 0 until SLOTS_SIZE) {
            if (slots[i] == null || !slots[i]!!.exists()) {
                edit.putString(slot_names[i], EMPTY)
                continue
            }
            try {
                edit.putString(slot_names[i], slots[i]!!.canonicalPath)
            } catch (e: IOException) {
                edit.putString(slot_names[i], EMPTY)
            }

        }
        edit.apply()
    }

    // Returns an adapter for last files list for LastFilesDialog
    fun getAdapter(activity: Activity): SimpleAdapter {
        val data = ArrayList<Map<String, Any>>()
        var m: MutableMap<String, Any>
        for (file in slots) {
            if (file != null) {
                m = HashMap()
                m["name"] = file.name
                m["path"] = file.path
                m["icon"] = R.drawable.file
                data.add(m)
            }
        }
        val from = arrayOf("name", "icon", "path")
        val to = intArrayOf(R.id.lastFilesItem_name, R.id.lastFilesItem_image, R.id.lastFilesItem_path)
        return SimpleAdapter(activity, data, R.layout.last_files_item, from, to)
    }

}