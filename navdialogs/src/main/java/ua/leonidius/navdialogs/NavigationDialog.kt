package ua.leonidius.navdialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel

import java.io.File

/**
 * This class is a base for OpenDialog and SaveDialog.
 */
abstract class NavigationDialog : DialogFragment(), OnItemClickListener {

    private var pathView: TextView? = null
    private var filesList: ListView? = null

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
        pathView = dialogView.findViewById(R.id.pathview)
        filesList = dialogView.findViewById(R.id.listview)
        filesList!!.onItemClickListener = this
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val header = inflater.inflate(R.layout.navdialogs_files_list_item, null, false)
        val imageView = header.findViewById<ImageView>(R.id.listitem_icon)
        imageView.setImageResource(R.drawable.up)
        val headerText = header.findViewById<TextView>(R.id.listitem_text)
        headerText.setText(R.string.up)
        filesList!!.addHeaderView(header)
    }

    override fun onItemClick(p1: AdapterView<*>, item: View, position: Int, p4: Long) {
        if (position == 0) {
            up()
            return
        }
        val path: String
        val name = (item.findViewById<View>(R.id.listitem_text) as TextView).text.toString()
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
            filesList!!.adapter = AdapterFactory.getFileAdapter(context!!, directory!!)
            getViewModel().currentDir = directory
            pathView!!.text = getViewModel().currentDir!!.path
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