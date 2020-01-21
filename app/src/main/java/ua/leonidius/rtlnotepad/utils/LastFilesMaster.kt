package ua.leonidius.rtlnotepad.utils

import android.app.Activity
import android.net.Uri
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
    private lateinit var slots: LinkedList<Uri?>
    private val slot_names = arrayOf("slot1", "slot2", "slot3", "slot4", "slot5")
    private const val EMPTY = "empty"
    private const val SLOTS_SIZE = 5

    fun add(uri: Uri) {
        // Checking if file is already on the list and placing it to the top in this case
        for (uriInSlot in slots) {
            if (uriInSlot === uri) {
                slots.remove(uriInSlot)
                slots.addFirst(uri)
                return
            }
        }

        if (slots.size == SLOTS_SIZE) slots.removeLast()
        slots.addFirst(uri)
    }

    // Retrieves recent files saved in Preferences. Called in main activity's onCreate()
    fun initSlots(activity: MainActivity) {
        slots = LinkedList()
        var path: String
        for (i in 0 until SLOTS_SIZE) {
            path = activity.pref.getString(slot_names[i], EMPTY) as String
            if (path != EMPTY) {
                // TODO: redo this so that file is not used
                slots.add(Uri.fromFile(File(path)))
            } else slots.add(null)
        }
    }

    // Saves recent files to the preferences. Called in main activity's onSaveInstanceState()
    fun saveSlots(activity: MainActivity) {
        val edit = activity.pref.edit()
        for (i in 0 until SLOTS_SIZE) {
            //if (slots[i] == null || !File(slots[i]!!.schemeSpecificPart).exists()) {
            if (slots[i] == null) {
                edit.putString(slot_names[i], EMPTY)
                continue
            }
            try {
                edit.putString(slot_names[i], slots[i]!!.schemeSpecificPart)
            } catch (e: IOException) {
                edit.putString(slot_names[i], EMPTY)
            }

        }
        edit.apply()
    }

    // Returns an adapter for last files list for LastFilesDialog
    fun getAdapter(activity: Activity): SimpleAdapter {
        val data = ArrayList<Map<String, Any>>()
        var map: MutableMap<String, Any>
        for (uri in slots) {
            if (uri != null) {
                map = HashMap()
                // TODO: save name together with path instead of checking it every time
                map["name"] = getFileName(activity.applicationContext, uri) as Any
                map["path"] = uri.path as Any
                map["icon"] = R.drawable.file
                data.add(map)
            }
        }
        val from = arrayOf("name", "icon", "path")
        val to = intArrayOf(R.id.lastFilesItem_name, R.id.lastFilesItem_image, R.id.lastFilesItem_path)
        return SimpleAdapter(activity, data, R.layout.last_files_item, from, to)
    }

}