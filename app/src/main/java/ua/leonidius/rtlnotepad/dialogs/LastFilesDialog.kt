package ua.leonidius.rtlnotepad.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.leonidius.rtlnotepad.R
import ua.leonidius.rtlnotepad.utils.LastFilesMaster

class LastFilesDialog : BaseDialog(), AdapterView.OnItemClickListener {

    private lateinit var viewModel : Model

    companion object {
        fun create(callback: (String) -> Unit) : LastFilesDialog {
            val dialog = LastFilesDialog()
            dialog.initializerFunction = {
                dialog.getViewModel().callback = callback
            }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setTitle(R.string.last_files)
        val lastFilesList = ListView(activity)
        lastFilesList.onItemClickListener = this
        lastFilesList.adapter = LastFilesMaster.getAdapter(activity as Activity)
        adb.setView(lastFilesList)
        return adb.create()
    }

    override fun onItemClick(p1: AdapterView<*>, item: View, p3: Int, p4: Long) {
        getViewModel().callback((item.findViewById<View>(R.id.lastFilesItem_path) as TextView).text.toString())
        dialog?.cancel()
    }

    private fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : ViewModel() {
        internal lateinit var callback: ((String) -> Unit)
    }

}