package ua.leonidius.rtlnotepad.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi

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

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun takePersistablePermissions(context: Context, uri: Uri) {
    val contentResolver = context.contentResolver
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    contentResolver.takePersistableUriPermission(uri, takeFlags)
}

fun canWriteFile(uri: Uri): Boolean {
    // TODO: implement
    return true
}