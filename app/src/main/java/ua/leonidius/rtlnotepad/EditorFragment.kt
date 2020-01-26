package ua.leonidius.rtlnotepad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import ua.leonidius.navdialogs.LegacySaveDialog
import ua.leonidius.rtlnotepad.dialogs.CloseTabDialog
import ua.leonidius.rtlnotepad.dialogs.ConfirmEncodingChangeDialog
import ua.leonidius.rtlnotepad.dialogs.EncodingDialog
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog
import ua.leonidius.rtlnotepad.utils.LastFilesMaster
import ua.leonidius.rtlnotepad.utils.ReadTask
import ua.leonidius.rtlnotepad.utils.WriteTask
import ua.leonidius.rtlnotepad.utils.getFileName

class EditorFragment : Fragment() {

    internal var mTag: String =  System.currentTimeMillis().toString()

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
            ReadTask(mActivity.contentResolver, it, currentEncoding) { text ->
                if (text == null) close()
                else {
                    setTextWithProgressDialog(text)
                    setTextChanged(false)
                }
            }.execute()
        }
        initialized = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.editor_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.options_save -> {
                if (uri == null)
                    openSaveDialog()
                else
                    writeFile(uri!!, currentEncoding) // Saving changes
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            LegacySaveDialog.create(defaultEncoding = currentEncoding, callback = writeFile)
                    .show(childFragmentManager, "saveDialogLegacy")
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
                putExtra(Intent.EXTRA_TITLE, ".txt")
            }
            startActivityForResult(intent, SAVE_FILE)
        }
    }

    private fun setTextChanged(changed: Boolean) {
        hasUnsavedChanges = changed
        val selectedTab = mActivity.actionBar!!.selectedTab
        val name: String = if (uri == null) getString(R.string.new_document) else getFileName(mActivity, uri!!) ?: getString(R.string.new_document)
        selectedTab.text = if (changed) "$name*" else name
    }

    private fun setEncoding(newEncoding: String) {
        if (uri == null) {
            currentEncoding = newEncoding
            return
        }

        val readFileAgain = {
            ReadTask(mActivity.contentResolver, uri!!, newEncoding) {
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

            if (uri != null) {
                writeFile(uri!!, currentEncoding) // Saving changes
                mActivity.closeTab(selectedTab)
                return@create
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                LegacySaveDialog.create(defaultEncoding = currentEncoding, callback = writeFileAndCloseTab)
                        .show(childFragmentManager, "saveDialogLegacy")
            } else {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                    putExtra(Intent.EXTRA_TITLE, ".txt")
                }
                startActivityForResult(intent, SAVE_FILE_AND_CLOSE)
            }

        }.show(childFragmentManager, "closeTabDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            SAVE_FILE -> resultData?.data?.let { writeFile(it, currentEncoding) }
            SAVE_FILE_AND_CLOSE -> resultData?.data?.let { writeFileAndCloseTab(it, currentEncoding) }
        }
    }

    private val writeFile: (Uri, String) -> Unit = { uri, encoding ->
        WriteTask(mActivity.contentResolver, uri, editor.text.toString(), encoding) {
            if (it) {
                this.uri = uri
                this.currentEncoding = encoding
                setTextChanged(false)
                val successMessage = resources.getString(R.string.file_save_success, getFileName(mActivity, uri))
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                LastFilesMaster.add(uri)
            } else {
                Toast.makeText(context, R.string.file_save_error, Toast.LENGTH_SHORT).show()
            }
        }.execute()
    }

    private val writeFileAndCloseTab: (Uri, String) -> Unit = { uri, encoding ->
        WriteTask(mActivity.contentResolver, uri, editor.text.toString(), encoding) {
            if (it) {
                val successMessage = resources.getString(R.string.file_save_success, getFileName(mActivity, uri))
                Toast.makeText(activity, successMessage, Toast.LENGTH_SHORT).show()
                LastFilesMaster.add(uri)
                with (mActivity) {
                    closeTab(actionBar!!.selectedTab)
                }
            } else {
                Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT).show()
            }
        }
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

    companion object {

        internal const val ARGUMENT_URI = "URI"

        // Request codes
        const val SAVE_FILE = 1
        const val SAVE_FILE_AND_CLOSE = 2
    }

}