package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.MainActivity
import ua.leonidius.rtlnotepad.R
import ua.leonidius.rtlnotepad.utils.EncodingAdapter

import java.nio.charset.Charset

class EncodingDialog : DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var viewModel: ViewModel
    private val initializer = Initializer()
    private lateinit var adapter: EncodingAdapter

    companion object {
        fun create(currentEncoding: String, callback: (String) -> Unit): EncodingDialog {
            val dialog = EncodingDialog()
            dialog.initializer.currentEncoding = currentEncoding
            dialog.initializer.callback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentEncoding = if (this::viewModel.isInitialized) getViewModel().currentEncoding
        else initializer.currentEncoding

        val adb = AlertDialog.Builder(MainActivity.instance)
        adb.setTitle(R.string.encoding)
        adb.setPositiveButton(R.string.apply, this)
        adb.setNegativeButton(R.string.cancel, this)

        val listView = ListView(MainActivity.instance)
        adapter = EncodingAdapter(MainActivity.instance, Charset.availableCharsets().keys.toTypedArray(), currentEncoding)
        listView.adapter = adapter
        adb.setView(listView)

        return adb.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializer.initialize()
    }

    override fun onClick(p1: DialogInterface, id: Int) {
        if (id == Dialog.BUTTON_POSITIVE) {
            getViewModel().callback(adapter.selectedEncoding)
        }
    }

    fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this)[viewModel.javaClass]
        }
        return viewModel as Model
    }

    class Model : ViewModel() {
        lateinit var currentEncoding: String
        lateinit var callback: (String) -> Unit
    }

    inner class Initializer {
        lateinit var currentEncoding: String
        lateinit var callback: (String) -> Unit

        fun initialize() {
            getViewModel().currentEncoding = currentEncoding
            getViewModel().callback = callback
        }
    }

}