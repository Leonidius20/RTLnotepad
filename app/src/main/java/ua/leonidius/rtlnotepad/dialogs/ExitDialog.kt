package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ua.leonidius.rtlnotepad.MainActivity
import ua.leonidius.rtlnotepad.R

class ExitDialog : DialogFragment(), DialogInterface.OnClickListener {

    // TODO("Rewrite with callback")

    override fun onClick(p1: DialogInterface, id: Int) {
        if (id == Dialog.BUTTON_POSITIVE) MainActivity.instance.finish()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(MainActivity.instance)
        adb.setCancelable(false)
        adb.setTitle(R.string.exit)
        adb.setMessage(R.string.unsaved_files)
        adb.setPositiveButton(R.string.yes, this)
        adb.setNegativeButton(R.string.no, this)
        return adb.create()
    }

}