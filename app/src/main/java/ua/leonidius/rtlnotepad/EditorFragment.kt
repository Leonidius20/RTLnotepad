package ua.leonidius.rtlnotepad

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import ua.leonidius.rtlnotepad.dialogs.*
import ua.leonidius.rtlnotepad.utils.LastFilesMaster
import ua.leonidius.rtlnotepad.utils.ReadTask
import ua.leonidius.rtlnotepad.utils.WriteTask

import java.io.File

class EditorFragment : Fragment() {

    var file: File? = null
    internal var hasUnsavedChanges = false

    //private String textToPaste = null;
    internal var mTag: String =  System.currentTimeMillis().toString()
    private var currentEncoding = "UTF-8"
    var editor: EditText? = null
        private set
    private var mActivity: MainActivity? = null
    private var initialized = false


    // Doesn't get called if fragment is recreated (setRetainInstance(true))
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setHasOptionsMenu(true)
        mActivity = activity as MainActivity?

        /*if (savedState != null) {
			// Restoration
			file = (File)savedState.getSerializable(BUNDLE_FILE);
			mTag = savedState.getString(BUNDLE_TAG, getTag());
			currentEncoding = savedState.getString(BUNDLE_CURRENT_ENCODING, "UTF-8");
			hasUnsavedChanges = savedState.getBoolean(BUNDLE_HAS_UNSAVED_CHANGES, false);
			initialized = true;
		}*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View? {
        // Initializing views
        val scrollView = inflater.inflate(R.layout.main, container, false)
        editor = scrollView.findViewById(R.id.editor)
        editor!!.textSize = mActivity!!.pref.getInt(mActivity!!.PREF_TEXT_SIZE, mActivity!!.SIZE_MEDIUM).toFloat()
        editor!!.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p1: CharSequence, p2: Int, p3: Int, p4: Int) {}

            override fun onTextChanged(p1: CharSequence, p2: Int, p3: Int, p4: Int) {}

            override fun afterTextChanged(p1: Editable) {
                setTextChanged(true)
            }

        })

        return scrollView
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (!initialized) { // Cold start
            currentEncoding = "UTF-8"

            val arguments = arguments
            if (arguments != null) {
                val filePath = arguments.getString(ARGUMENT_FILE_PATH, null)
                if (filePath != null) file = File(filePath)

                if (file != null)
                    readFile(file!!, currentEncoding) { text ->
                        if (text == null)
                            close() // Close if failed to read requested file
                        else
                            setTextWithProgressDialog(text)
                    }
            }
            initialized = true
        } else {
            // getting the reference to the new activity, that was created after configuration change
            mActivity = getActivity() as MainActivity?
        }
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
            writeFile(file, editor!!.text.toString(), encoding) { success ->
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
        writeFile(file!!, editor!!.text.toString(), currentEncoding) { success ->
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
        val selectedTab = mActivity!!.actionBar!!.selectedTab

        val name: String
        if (file == null)
            name = getString(R.string.new_document)
        else
            name = file!!.name

        if (changed)
            selectedTab.text = "$name*"
        else
            selectedTab.text = name
    }

    private fun setEncoding(newEncoding: String) {
        if (file == null) {
            currentEncoding = newEncoding
            return
        }

        if (!hasUnsavedChanges) {
            readFile(file!!, newEncoding) { result ->
                if (result != null) {
                    editor!!.setText(result)
                    currentEncoding = newEncoding
                } else {
                    Toast.makeText(activity, R.string.reading_error, Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        ConfirmEncodingChangeDialog.create { change ->
            if (change) {
                readFile(file!!, newEncoding) { result ->
                    if (result != null) {
                        editor!!.setText(result)
                        currentEncoding = newEncoding
                    } else {
                        Toast.makeText(activity, R.string.reading_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.show(childFragmentManager, "confirmEncodingChangeDialog")
    }

    /**
     * Closes the tab to which the EditorFragment is assigned.
     * Provides an opportunity to save changes if they are not saved.
     */
    private fun close() {
        val selectedTab = mActivity!!.actionBar!!.selectedTab

        if (!hasUnsavedChanges) {
            mActivity!!.closeTab(selectedTab)
            return
        }

        CloseTabDialog.create { save ->
            if (!save) {
                mActivity!!.closeTab(selectedTab)
                return@create
            }

            if (file != null) {
                saveChanges()
                mActivity!!.closeTab(selectedTab)
                return@create
            }

            SaveDialog.create { file, encoding ->
                writeFile(file, editor!!.text.toString(), encoding) { success ->
                    if (success) {
                        val successMessage = resources.getString(R.string.file_save_success, file.name)
                        Toast.makeText(activity, successMessage, Toast.LENGTH_SHORT).show()
                        LastFilesMaster.add(file)
                        mActivity!!.closeTab(selectedTab)
                    } else {
                        Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }.show(childFragmentManager, "saveDialog")

        }.show(childFragmentManager, "closeTabDialog")
    }

    /**
     * Sets a specified text to editor and shows a progress dialog while it is being set.
     * @param text Text to set
     */
    private fun setTextWithProgressDialog(text: CharSequence?) {
        val dialog = LoadingDialog()
        dialog.show(childFragmentManager, "loadingDialog")
        editor!!.setText(text)
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
    private fun readFile(file: File, encoding: String, callback: (String) -> Unit) {
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
    }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
    }

    fun tryReadingFileAgain() {
        readFile(fileToRead, encodingForReading, readCallback)
    }

    fun tryWritingFileAgain() {
        writeFile(fileToWrite, textToWrite, encodingForWriting, writeCallback)
    }

    companion object {

        internal val ARGUMENT_FILE_PATH = "filePath" // will be removed once ViewModel is separated from the fragment
        //private val BUNDLE_FILE = "file"
        //private val BUNDLE_TAG = "tag"
        //private val BUNDLE_CURRENT_ENCODING = "currentEncoding"
        //private val BUNDLE_HAS_UNSAVED_CHANGES = "hasUnsavedChanges"

        val READ_PERMISSION_CODE = 0
        val WRITE_PERMISSION_CODE = 1
    }

}

/* TODO: if the process was killed while a dialogFragment was open, dismiss the dialog
*  we might pass null to super.onCreate(Bundle savedState) and handle everything ourselves.
* in such a case the dialog fragments will not be retained
 */