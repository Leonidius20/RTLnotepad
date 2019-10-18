package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ua.leonidius.rtlnotepad.R

class WrongFileTypeDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var callback: Callback? = null // TODO("ViewModel")

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> callback!!.callback(false)
            Dialog.BUTTON_POSITIVE -> callback!!.callback(true)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setCancelable(false)
        adb.setMessage(R.string.wrong_file_type_warning)
        adb.setNegativeButton(android.R.string.no, this)
        adb.setPositiveButton(android.R.string.yes, this)
        return adb.create()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {
        fun callback(open: Boolean)
    }

}