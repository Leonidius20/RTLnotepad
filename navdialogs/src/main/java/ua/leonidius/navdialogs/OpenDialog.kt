package ua.leonidius.navdialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.ViewModelProvider

import java.io.File

class OpenDialog : NavigationDialog() {

    private lateinit var viewModel: Model
    private var initializerFunction : (() -> Unit)? = null

    companion object {

        fun create(defaultDir: File = Environment.getExternalStorageDirectory(), callback: (File) -> Unit): OpenDialog {
            val dialog = OpenDialog()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializerFunction?.invoke()
        initializerFunction = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO: instead of a header put a horizontal layout with "up" and "create folder" options
        return inflater.inflate(R.layout.navdialogs_dialog_open, container)
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

    /*private boolean isText(File file) {
        try {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())).split("/")[0].equals("text");
        } catch (Exception e) {
            return false;
        }
    }*/

    class Model : NavigationDialog.NavDialogViewModel() {
        internal lateinit var callback: ((File) -> Unit)
    }

}