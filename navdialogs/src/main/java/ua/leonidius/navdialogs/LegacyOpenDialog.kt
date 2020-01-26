package ua.leonidius.navdialogs

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.ViewModelProvider

import java.io.File

class LegacyOpenDialog : NavigationDialog() {

    private lateinit var viewModel: Model

    companion object {

        fun create(defaultDir: File = Environment.getExternalStorageDirectory(), callback: (File) -> Unit): LegacyOpenDialog {
            val dialog = LegacyOpenDialog()
            dialog.initializerFunction = {
                with (dialog.getViewModel()) {
                    this.currentDir = defaultDir
                    this.callback = callback
                }
            }
            return dialog
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO: instead of a header put a horizontal layout with "up" and "create folder" options
        return inflater.inflate(R.layout.nav_dialogs_open, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        openDir(getViewModel().currentDir)
    }

    override fun onFileClick(file: File) {
        getViewModel().callback(file)
        dialog!!.dismiss()
    }

    override fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : NavigationDialog.NavDialogViewModel() {
        internal lateinit var callback: ((File) -> Unit)
    }

}