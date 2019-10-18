package ua.leonidius.navdialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RewriteDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var viewModel : Model? = null;
    private val initializer = Initializer()

    companion object {

        fun create(callback: (Boolean) -> Unit): RewriteDialog {
            val dialog = RewriteDialog()
            dialog.initializer.callback = callback
            return dialog
        }

    }

    override fun onClick(p1: DialogInterface, id: Int) {
        when (id) {
            Dialog.BUTTON_NEGATIVE -> getViewModel().callback!!.invoke(false)
            Dialog.BUTTON_POSITIVE -> getViewModel().callback!!.invoke(true)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!getViewModel().initialized) {
            initializer.initialize()
            getViewModel().initialized = true
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
        if (viewModel == null) {
            viewModel = ViewModelProvider(this).get(Model::class.java!!)
        }
        return viewModel as Model
    }

    class Model : ViewModel() {
        internal var initialized = false
        internal var callback: ((Boolean) -> Unit)? = null
    }

    inner class Initializer {
        internal var callback: ((Boolean) -> Unit)? = null

        internal fun initialize() {
            getViewModel().callback = callback
        }
    }

}