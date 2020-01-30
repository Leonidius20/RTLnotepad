package ua.leonidius.rtlnotepad.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

fun getFileName(context: Context, uri: Uri) : String? {
    val contentResolver = context.contentResolver
    var output: String? = null
    try {
        val cursor: Cursor? = contentResolver.query(
                uri, null, null, null, null, null)
        cursor?.use {
            output = if (!it.moveToFirst()) null
            else it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        return null
    }
    if (output == null && uri.scheme == "file") {
        output = uri.lastPathSegment
    }
    return output
}

fun takePersistablePermissions(context: Context, uri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val contentResolver = context.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }
}

fun canWriteFile(context: Context, uri: Uri): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        return true
    }

    // If no writing permissions...
    if (context.checkCallingOrSelfUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            != PackageManager.PERMISSION_GRANTED) return false;

    val contentResolver = context.contentResolver
    try {
        val cursor = contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
                null, null, null)
        cursor?.use {
            return if (it.moveToFirst() && !it.isNull(0)) {
                val flags = cursor.getInt(0)
                (flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0
            } else false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return false
}

fun fileExists(context: Context, uri: Uri): Boolean {
    val resolver = context.contentResolver
    try {
        val cursor = resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                null, null, null)
        cursor?.use {
            return cursor.count > 0
        }
        return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

// For LegacyOpenDialog's WrongFileTypeDialog
fun isText(uri: Uri): Boolean {
    return try {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))!!.split("/")[0] == "text";
    } catch (e: Exception) {
        false
    }
}