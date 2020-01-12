package ua.leonidius.navdialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.nio.charset.Charset
import java.util.*

class SaveDialog : NavigationDialog(), DialogInterface.OnClickListener {

    private lateinit var nameField: EditText
    private lateinit var encodingSpinner: Spinner
    private lateinit var viewModel: Model
    private var initializerFunction : (() -> Unit)? = null

    companion object {

        fun create(defaultName: String = ".txt", defaultEncoding: String = "UTF-8",
                   defaultDirectory: File = Environment.getExternalStorageDirectory(),
                   callback: (File, String) -> Unit): SaveDialog {
            val dialog = SaveDialog()
            dialog.initializerFunction = {
                with(dialog.getViewModel()) {
                    this.fileName = defaultName
                    this.currentEncoding = defaultEncoding
                    this.currentDir = defaultDirectory
                    this.callback = callback
                }
            }
            return dialog
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializerFunction?.invoke()
        initializerFunction = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity)
        adb.setTitle(R.string.save_as)
        adb.setPositiveButton(android.R.string.ok, this)

        val inflater =  LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.navdialogs_dialog_save_as, null, false)
        initView(dialogView)
        adb.setView(dialogView)

        openDir(getViewModel().currentDir)

        val dialog = adb.create()
        setMaxHeight(dialog)
        return dialog
    }

    override fun initView(dialogView: View) {
        super.initView(dialogView)

        nameField = dialogView.findViewById(R.id.nameField)
        nameField.setText(getViewModel().fileName)

        encodingSpinner = dialogView.findViewById(R.id.encodingSpinner)

        val availableEncodings = getViewModel().getAvailableEncodings()

        val data = ArrayList<Map<String, Any>>()
        var m: MutableMap<String, Any>
        var currentEncodingPosition = 0
        for (i in availableEncodings.indices) {
            val encoding = availableEncodings[i]
            m = HashMap()
            m["name"] = encoding
            if (encoding.equals(getViewModel().currentEncoding, ignoreCase = true)) {
                currentEncodingPosition = i
            }
            data.add(m)
        }
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)

        encodingSpinner.adapter = SimpleAdapter(activity, data, android.R.layout.simple_list_item_1, from, to)
        encodingSpinner.setSelection(currentEncodingPosition)
    }

    override fun onClick(dialog: DialogInterface, id: Int) {
        val file = File(getViewModel().currentDir!!.path + File.separatorChar + nameField.text)
        val encoding = getViewModel().getAvailableEncodings()[encodingSpinner.selectedItemPosition]

        if (!file.exists()) {
            getViewModel().callback(file, encoding)
            return
        }

        RewriteDialog.create { rewrite ->
            if (rewrite)
                getViewModel().callback(file, encoding)
            else
                show(parentFragmentManager, "saveDialog")
        }.show(parentFragmentManager, "rewriteDialog")
    }

    override fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : NavigationDialog.NavDialogViewModel() {
        internal lateinit var callback: ((File, String) -> Unit)
        internal lateinit var fileName: String
        internal lateinit var currentEncoding: String

        private lateinit var availableEncodings: Array<String>

        internal fun getAvailableEncodings(): Array<String> {
            if (!this::availableEncodings.isInitialized) {
                availableEncodings = Charset.availableCharsets().keys.toTypedArray()
            }
            return availableEncodings
        }
    }

}