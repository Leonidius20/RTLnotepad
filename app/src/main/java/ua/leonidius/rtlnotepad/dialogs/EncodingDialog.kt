package ua.leonidius.rtlnotepad.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ListView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.R
import ua.leonidius.rtlnotepad.utils.EncodingAdapter
import java.nio.charset.Charset

class EncodingDialog : BaseDialog(), DialogInterface.OnClickListener {

    private lateinit var viewModel: ViewModel
    private lateinit var adapter: EncodingAdapter

    companion object {
        fun create(currentEncoding: String, callback: (String) -> Unit): EncodingDialog {
            val dialog = EncodingDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().currentEncoding = currentEncoding
                dialog.getViewModel().callback = callback
            }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setTitle(R.string.encoding)
        adb.setPositiveButton(R.string.apply, this)
        adb.setNegativeButton(R.string.cancel, this)

        val listView = ListView(activity)
        val currentEncoding = getViewModel().currentEncoding // viewModel must already be initialized by this time
        adapter = EncodingAdapter(activity as Activity, Charset.availableCharsets().keys.toTypedArray(), currentEncoding)
        listView.adapter = adapter
        adb.setView(listView)

        return adb.create()
    }

    override fun onClick(p1: DialogInterface, id: Int) {
        if (id == Dialog.BUTTON_POSITIVE) {
            getViewModel().callback(adapter.selectedEncoding)
        }
    }

    fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel as Model
    }

    class Model : ViewModel() {
        lateinit var currentEncoding: String
        lateinit var callback: (String) -> Unit
    }

}