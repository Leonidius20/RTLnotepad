package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import ua.leonidius.rtlnotepad.R

class LoadingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("LoadingDialog", "Inside onCreateDialog")
        val pdb = AlertDialog.Builder(activity)
        pdb.setTitle(R.string.processing)
        pdb.setMessage(R.string.processing)
        pdb.setCancelable(false)
        return pdb.create()
    }

}