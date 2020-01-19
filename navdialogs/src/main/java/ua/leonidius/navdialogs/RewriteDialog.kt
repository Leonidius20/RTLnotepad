package ua.leonidius.navdialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RewriteDialog : BaseDialog(), DialogInterface.OnClickListener {

    private lateinit var viewModel : Model

    companion object {

        fun create(callback: (Boolean) -> Unit): RewriteDialog {
            val dialog = RewriteDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().callback = callback
            }
            return dialog
        }

    }

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> getViewModel().callback.invoke(false)
            Dialog.BUTTON_POSITIVE -> getViewModel().callback.invoke(true)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setCancelable(false)
        adb.setMessage(R.string.file_overwrite)
        adb.setNegativeButton(android.R.string.no, this)
        adb.setPositiveButton(android.R.string.yes, this)
        return adb.create()
    }

    private fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : ViewModel() {
        internal lateinit var callback: ((Boolean) -> Unit)
    }

}