package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.R
import ua.leonidius.rtlnotepad.utils.LastFilesAdapter

class LastFilesDialog : BaseDialog(), AdapterView.OnItemClickListener {

    private lateinit var viewModel: Model

    companion object {
        fun create(callback: (Uri) -> Unit): LastFilesDialog {
            return LastFilesDialog().apply {
                initializerFunction = { getViewModel().callback = callback }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setTitle(R.string.last_files)
        val lastFilesList = ListView(activity)
        lastFilesList.onItemClickListener = this
        lastFilesList.adapter = LastFilesAdapter(activity!!)
        adb.setView(lastFilesList)
        return adb.create()
    }

    override fun onItemClick(p1: AdapterView<*>, item: View, p3: Int, p4: Long) {
        getViewModel().callback(item.tag as Uri)
        dialog?.cancel()
    }

    private fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : ViewModel() {
        internal lateinit var callback: ((Uri) -> Unit)
    }

}