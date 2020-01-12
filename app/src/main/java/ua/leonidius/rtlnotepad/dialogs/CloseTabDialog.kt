package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.MainActivity
import ua.leonidius.rtlnotepad.R

/**
 * This dialog is showed when user tries to close a tab with unsaved changes.
 */

class CloseTabDialog : DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var viewModel: ViewModel
    private var initializerFunction : (() -> Unit)? = null

    companion object {
        fun create(callback : (Boolean) -> Unit) : CloseTabDialog {
            val dialog = CloseTabDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().callback = callback
            }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(MainActivity.instance)
        adb.setMessage(R.string.close_tab_warning)
        adb.setNegativeButton(R.string.no, this)
        adb.setPositiveButton(R.string.yes, this)
        adb.setNeutralButton(R.string.cancel, this)
        return adb.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializerFunction?.invoke()
        initializerFunction = null
    }

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> getViewModel().callback(false)
            Dialog.BUTTON_POSITIVE -> getViewModel().callback(true)
        }
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