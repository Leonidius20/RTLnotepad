package ua.leonidius.rtlnotepad

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ua.leonidius.navdialogs.SaveDialog
import ua.leonidius.rtlnotepad.dialogs.CloseTabDialog
import ua.leonidius.rtlnotepad.dialogs.ConfirmEncodingChangeDialog
import ua.leonidius.rtlnotepad.dialogs.EncodingDialog
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog
import ua.leonidius.rtlnotepad.utils.LastFilesMaster
import ua.leonidius.rtlnotepad.utils.ReadTask
import ua.leonidius.rtlnotepad.utils.WriteTask
import java.io.File

class EditorFragment : Fragment() {

    internal var mTag: String =  System.currentTimeMillis().toString()

    var file: File? = null
    var uri: Uri? = null
    private var currentEncoding = "UTF-8"
    internal var hasUnsavedChanges = false

    private lateinit var editor: EditText
    private lateinit var mActivity: MainActivity

    private var initialized = false
    private var ignoreNextTextChange = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as MainActivity
        // If it's not a new empty editor, don't trigger afterTextChanged() when setting the text
        if (arguments != null || initialized) ignoreNextTextChange = true
    }

    // Doesn't get called if fragment is recreated (setRetainInstance(true))
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View? {
        val scrollView = inflater.inflate(R.layout.main, container, false)

        editor = scrollView.findViewById(R.id.editor)
        editor.apply {
            textSize =  mActivity.pref.getInt(MainActivity.PREF_TEXT_SIZE, MainActivity.SIZE_MEDIUM).toFloat()
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p1: CharSequence, p2: Int, p3: Int, p4: Int) {}
                override fun onTextChanged(p1: CharSequence, p2: Int, p3: Int, p4: Int) {}
                override fun afterTextChanged(p1: Editable) {
                    if (!ignoreNextTextChange) setTextChanged(true)
                    else ignoreNextTextChange = false
                }
            })
        }

        return scrollView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (initialized) return
        arguments?.getParcelable<Uri>(ARGUMENT_URI)?.also {
            ReadTask(it, currentEncoding) { text ->
                if (text == null) close()
                else {
                    setTextWithProgressDialog(text)
                    setTextChanged(false)
                }
            }.execute()
        }
        initialized = true

        /*val arguments = arguments
        if (arguments != null) {
            val filePath = arguments.getString(ARGUMENT_FILE_PATH, null)
            if (filePath != null) file = File(filePath)

            if (file != null) {
                readFile(file!!, currentEncoding) { text ->
                    if (text == null)
                        close() // Close if failed to read requested file
                    else
                        setTextWithProgressDialog(text)
                        setTextChanged(false)
                }
            }

        }*/
        //initialized = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.editor_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.options_save -> {
                if (file == null)
                    openSaveDialog()
                else
                    saveChanges()
                return true
            }
            R.id.options_save_as -> {
                openSaveDialog()
                return true
            }
            R.id.options_encoding -> {
                EncodingDialog.create(currentEncoding, this::setEncoding)
                        .show(childFragmentManager, "encodingDialog")
                return true
            }
            R.id.options_close -> {
                close()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Shows a SaveDialog and writes the text to the selected file.
     */
    private fun openSaveDialog() {
        SaveDialog.create { file, encoding ->
            writeFile(file, editor.text.toString(), encoding) { success ->
                if (success) {
                    this.file = file
                    this.currentEncoding = encoding
                    setTextChanged(false)
                    val successMessage = resources.getString(R.string.file_save_success, file.name)
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    LastFilesMaster.add(file)
                } else {
                    Toast.makeText(context, R.string.file_save_error, Toast.LENGTH_SHORT).show()
                }
            }
        }.show(childFragmentManager, "saveDialog")
    }

    /**
     * Saves changes in the current file. Doesn't check if the file equals null.
     */
    private fun saveChanges() {
        writeFile(file!!, editor.text.toString(), currentEncoding) { success ->
            if (success) {
                setTextChanged(false)
                val successMessage = resources.getString(R.string.file_save_success, file!!.name)
                Toast.makeText(activity, successMessage, Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTextChanged(changed: Boolean) {
        hasUnsavedChanges = changed
        val selectedTab = mActivity.actionBar!!.selectedTab
        val name: String = if (file == null) getString(R.string.new_document) else file!!.name
        selectedTab.text = if (changed) "$name*" else name
    }

    private fun setEncoding(newEncoding: String) {
        if (uri == null) {
            currentEncoding = newEncoding
            return
        }

        val readFileAgain = {
            ReadTask(uri!!, newEncoding) {
                if (it != null) {
                    editor.setText(it)
                    currentEncoding = newEncoding
                } else Toast.makeText(activity, R.string.reading_error, Toast.LENGTH_SHORT).show()
            }.execute()
        }

        if (hasUnsavedChanges) {
            ConfirmEncodingChangeDialog.create {
                if (it) readFileAgain()
            }.show(childFragmentManager, "confirmEncodingChangeDialog")
        } else readFileAgain()
    }

    /**
     * Closes the tab to which the EditorFragment is assigned.
     * Provides an opportunity to save changes if they are not saved.
     */
    private fun close() {
        val selectedTab = mActivity.actionBar!!.selectedTab

        if (!hasUnsavedChanges) {
            mActivity.closeTab(selectedTab)
            return
        }

        CloseTabDialog.create { save ->
            if (!save) {
                mActivity.closeTab(selectedTab)
                return@create
            }

            if (file != null) {
                saveChanges()
                mActivity.closeTab(selectedTab)
                return@create
            }

            SaveDialog.create { file, encoding ->
                writeFile(file, editor.text.toString(), encoding) { success ->
                    if (success) {
                        val successMessage = resources.getString(R.string.file_save_success, file.name)
                        Toast.makeText(activity, successMessage, Toast.LENGTH_SHORT).show()
                        LastFilesMaster.add(file)
                        mActivity.closeTab(selectedTab)
                    } else {
                        Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }.show(childFragmentManager, "saveDialog")

        }.show(childFragmentManager, "closeTabDialog")
    }

    fun setEditorTextSize(size: Float) {
        editor.textSize = size
    }

    /**
     * Sets a specified text to editor and shows a progress dialog while it is being set.
     * @param text Text to set
     */
    private fun setTextWithProgressDialog(text: CharSequence?) {
        val dialog = LoadingDialog()
        dialog.show(childFragmentManager, "loadingDialog")
        editor.setText(text)
        dialog.dismiss()
    }

    private lateinit var fileToRead: File
    private lateinit var encodingForReading: String
    private lateinit var readCallback: (String) -> Unit

    /**
     * Asynchronously reads a specified file into a string and returns it via a callback.
     * Requests reading permission. Shows a LoadingDialog in the process.
     *
     * @param file     File to read
     * @param encoding Encoding to use for decoding of the file
     * @param callback Defines what to do with the results of the operation
     */
    /*private fun readFile(file: File, encoding: String, callback: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("RTLnotepad", "No read permission, requesting...")
            // saving data to use in onRequestPermissionsResult()
            fileToRead = file
            encodingForReading = encoding
            readCallback = callback
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_CODE)
            return
        }
        val dialog = LoadingDialog()
        dialog.show(childFragmentManager, "loadingDialog")
        val task = ReadTask(file, encoding) { result ->
            dialog.dismiss()
            callback.invoke(result)
        }
        task.execute()
    }*/

    private lateinit var fileToWrite: File
    private lateinit var textToWrite: String
    private lateinit var encodingForWriting: String
    private lateinit var writeCallback: (Boolean) -> Unit

    /**
     * Asynchronously writes a file to the disk. Returns the status (success/failure) via
     * a callback. Requests writing permission. Shows a LoadingDialog in the process.
     *
     * @param file     File to write into
     * @param text     Text to write into the file
     * @param encoding Encoding to use
     * @param callback Defines what to do after the operation
     */
    private fun writeFile(file: File, text: String, encoding: String, callback: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("RTLnotepad", "No write permission, requesting...")
            // saving data to use in onRequestPermissionsResult()
            fileToWrite = file
            textToWrite = text
            encodingForWriting = encoding
            writeCallback = callback
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION_CODE)
            return
        }
        val dialog = LoadingDialog()
        dialog.show(childFragmentManager, "loadingDialog")
        val task = WriteTask(file, text, encoding) { success ->
            dialog.dismiss()
            callback.invoke(success)
        }
        task.execute()
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                READ_PERMISSION_CODE -> tryReadingFileAgain()
                WRITE_PERMISSION_CODE -> tryWritingFileAgain()
            }
        } else {
            val dialog = PermissionRequestDialog()
            val args = Bundle()
            args.putInt(PermissionRequestDialog.TYPE, requestCode)
            dialog.arguments = args
            dialog.show(childFragmentManager, "permissionRequestDialog")
        }
    }*/

    /*fun tryReadingFileAgain() {
        readFile(fileToRead, encodingForReading, readCallback)
    }*/

    fun tryWritingFileAgain() {
        writeFile(fileToWrite, textToWrite, encodingForWriting, writeCallback)
    }

    companion object {

        internal const val ARGUMENT_FILE_PATH = "filePath" // will be removed once ViewModel is separated from the fragment
        internal const val ARGUMENT_URI = "URI"
        //private val BUNDLE_FILE = "file"
        //private val BUNDLE_TAG = "tag"
        //private val BUNDLE_CURRENT_ENCODING = "currentEncoding"
        //private val BUNDLE_HAS_UNSAVED_CHANGES = "hasUnsavedChanges"

        const val READ_PERMISSION_CODE = 0
        const val WRITE_PERMISSION_CODE = 1
    }

}

/* TODO: if the process was killed while a dialogFragment was open, dismiss the dialog
*  we might pass null to super.onCreate(Bundle savedState) and handle everything ourselves.
* in such a case the dialog fragments will not be retained
 */