package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.R

class ConfirmEncodingChangeDialog : BaseDialog(), DialogInterface.OnClickListener {

    private lateinit var viewModel: Model

    companion object {
        fun create(callback : (Boolean) -> Unit) : ConfirmEncodingChangeDialog {
            val dialog = ConfirmEncodingChangeDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().callback = callback
            }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setMessage(R.string.encoding_change_warning)
        adb.setNegativeButton(R.string.no, this)
        adb.setPositiveButton(R.string.yes, this)
        adb.setNeutralButton(R.string.cancel, this)
        return adb.create()
    }

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> getViewModel().callback(false)
            Dialog.BUTTON_POSITIVE -> getViewModel().callback(true)
        }
    }

    fun getViewModel() : Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : ViewModel() {
        lateinit var callback : (Boolean) -> Unit
    }

}