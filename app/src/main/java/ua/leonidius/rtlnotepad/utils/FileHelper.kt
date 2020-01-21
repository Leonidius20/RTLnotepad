package ua.leonidius.rtlnotepad.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri) : String? {
    val contentResolver = context.contentResolver
    val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)
    cursor?.use {
        if (!it.moveToFirst()) return@use
        return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return null
}