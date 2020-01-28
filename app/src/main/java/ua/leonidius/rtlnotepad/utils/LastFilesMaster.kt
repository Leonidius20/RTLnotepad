package ua.leonidius.rtlnotepad.utils

import android.net.Uri
import ua.leonidius.rtlnotepad.Settings

/**
 * This file manages the list of last opened files.
 */
private const val SLOTS_SIZE = 5

fun addToLastFiles(uri: Uri) {
    with (Settings.lastFiles) {
        add(uri.toString())
        if (size == SLOTS_SIZE) remove(first())
    }
}