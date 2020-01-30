package ua.leonidius.navdialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.nio.charset.Charset
import java.util.*

class LegacySaveDialog : NavigationDialog(), DialogInterface.OnClickListener {

    private lateinit var nameField: EditText
    private lateinit var encodingSpinner: Spinner
    private lateinit var viewModel: Model

    companion object {

        fun create(defaultName: String = ".txt", defaultEncoding: String = "UTF-8",
                   defaultDirectory: File = Environment.getExternalStorageDirectory(),
                   callback: (Uri, String) -> Unit): LegacySaveDialog {
            val dialog = LegacySaveDialog()
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity as Activity)
        adb.setTitle(R.string.save_as)
        adb.setPositiveButton(android.R.string.ok, this)

        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.nav_dialogs_save_as_legacy, null, false)
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
            getViewModel().callback(Uri.fromFile(file), encoding)
            return
        }

        lateinit var rewriteDialog : RewriteDialog
        rewriteDialog = RewriteDialog.create { rewrite ->
            if (rewrite)
                getViewModel().callback(Uri.fromFile(file), encoding)
            else {
                // crash when activity was recreated after orientation change
                // technically we can use MainActivity.instance here...
                if (rewriteDialog.activity != null) {
                    show(rewriteDialog.activity!!.supportFragmentManager, "saveDialog")
                } else {
                    Log.d("SaveDialog", "There is no reference to the new activity, so the SaveDialog couldn't be shown")
                }
            }
            rewriteDialog.dialog?.cancel()
        }
        rewriteDialog.show(parentFragmentManager, "rewriteDialog")
    }

    override fun getViewModel(): Model {
        if (!this::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(Model::class.java)
        }
        return viewModel
    }

    class Model : NavigationDialog.NavDialogViewModel() {
        internal lateinit var callback: ((Uri, String) -> Unit)
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