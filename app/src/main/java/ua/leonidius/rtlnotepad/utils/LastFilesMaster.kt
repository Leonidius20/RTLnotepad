package ua.leonidius.rtlnotepad.utils

import android.net.Uri
import ua.leonidius.rtlnotepad.MainActivity

/**
 * This class manages the list of last opened files.
 */
object LastFilesMaster {
    // TODO: don't hold URIs in memory
    private lateinit var slots: LinkedHashSet<String>

    private const val SLOTS_SIZE = 5
    private const val PREF_LAST_FILES = "lastFiles"

    fun add(uri: Uri) {
        with (slots) {
            add(uri.toString())
            if (size == SLOTS_SIZE) remove(first())
        }
    }

    // Retrieves recent files saved in Preferences. Called in main activity's onCreate()
    fun initSlots(activity: MainActivity) {
        slots = LinkedHashSet(activity.pref.getStringSet(PREF_LAST_FILES, LinkedHashSet<String>())!!)
    }

    // Saves recent files to the preferences. Called in main activity's onSaveInstanceState()
    fun saveSlots(activity: MainActivity) {
        with (activity.pref.edit()) {
            putStringSet(PREF_LAST_FILES, slots)
            apply()
        }
    }

    fun getLastFiles(): LinkedHashSet<String> {
        return slots
    }

}