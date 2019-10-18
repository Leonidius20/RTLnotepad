package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ua.leonidius.rtlnotepad.R

class LoadingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pdb = AlertDialog.Builder(activity)
        // TODO("Show progress bar")
        pdb.setMessage(R.string.processing)
        pdb.setCancelable(false)
        return pdb.create()
    }

}