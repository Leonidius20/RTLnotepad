package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.R

class WrongFileTypeDialog : BaseDialog(), DialogInterface.OnClickListener {

    private lateinit var viewModel: ViewModel

    companion object {
        fun create(callback : (Boolean) -> Unit) : WrongFileTypeDialog {
            val dialog = WrongFileTypeDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().callback = callback
            }
            return dialog
        }
    }

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> getViewModel().callback(false)
            Dialog.BUTTON_POSITIVE -> getViewModel().callback(true)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setCancelable(false)
        adb.setMessage(R.string.wrong_file_type_warning)
        adb.setNegativeButton(R.string.no, this)
        adb.setPositiveButton(R.string.yes, this)
        return adb.create()
    }

    private fun getViewModel() : Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel as Model
    }

    class Model : ViewModel() {
        lateinit var callback : (Boolean) -> Unit
    }

}