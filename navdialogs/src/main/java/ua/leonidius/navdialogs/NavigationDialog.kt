package ua.leonidius.navdialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.lifecycle.ViewModel
import java.io.File

/**
 * This class is a base for OpenDialog and SaveDialog.
 */
abstract class NavigationDialog : BaseDialog(), OnItemClickListener {

    private lateinit var pathView: TextView
    private lateinit var filesList: ListView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setMaxHeight(dialog)
        return dialog
    }

    fun setMaxHeight(dialog: Dialog) {
        val params = WindowManager.LayoutParams()
        params.copyFrom(dialog.window!!.attributes)
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = params
    }

    protected open fun initView(dialogView: View) {
        pathView = dialogView.findViewById(R.id.pathView)
        filesList = dialogView.findViewById(R.id.filesListView)
        filesList.onItemClickListener = this
        val inflater = LayoutInflater.from(context)
        val header = inflater.inflate(R.layout.navdialogs_files_list_item, null, false)
        val imageView = header.findViewById(R.id.listItemIcon) as ImageView
        imageView.setImageResource(R.drawable.up)
        val headerText = header.findViewById(R.id.listItemText) as TextView
        headerText.setText(R.string.up)
        filesList.addHeaderView(header)
    }

    override fun onItemClick(p1: AdapterView<*>, item: View, position: Int, p4: Long) {
        if (position == 0) {
            up()
            return
        }
        val path: String
        val name = (item.findViewById<View>(R.id.listItemText) as TextView).text.toString()
        if (getViewModel().currentDir!!.path == "/")
            path = getViewModel().currentDir!!.path + name
        else
            path = getViewModel().currentDir!!.path + "/" + name
        val file = File(path)
        if (file.isDirectory)
            openDir(file)
        else
            onFileClick(file)
    }

    protected open fun onFileClick(file: File) {}

    fun openDir(directory: File?) {
        try {
            filesList.adapter = getFileAdapter(activity as Activity, directory!!)
            getViewModel().currentDir = directory
            pathView.text = getViewModel().currentDir!!.path
        } catch (e: Exception) {
            Toast.makeText(activity, R.string.folder_open_error, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    private fun up() {
        if (getViewModel().currentDir!!.path == "/") return
        getViewModel().currentDir = getViewModel().currentDir!!.parentFile
        openDir(getViewModel().currentDir)
    }

    abstract fun getViewModel(): NavDialogViewModel

    abstract class NavDialogViewModel : ViewModel() {
        var currentDir: File? = null
    }

}