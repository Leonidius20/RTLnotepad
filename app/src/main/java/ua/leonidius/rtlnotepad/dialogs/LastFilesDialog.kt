package ua.leonidius.rtlnotepad.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ua.leonidius.rtlnotepad.MainActivity
import ua.leonidius.rtlnotepad.R
import ua.leonidius.rtlnotepad.utils.LastFilesMaster

class LastFilesDialog : DialogFragment(), AdapterView.OnItemClickListener {

    private lateinit var callback: (String) -> Unit // TODO("Replace with ViewModel")

    override fun onItemClick(p1: AdapterView<*>, item: View, p3: Int, p4: Long) {
        callback((item.findViewById<View>(R.id.lastFilesItem_path) as TextView).text.toString())
        dialog?.cancel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(MainActivity.instance)
        adb.setTitle(R.string.last_files)
        val l = ListView(MainActivity.instance)
        l.onItemClickListener = this
        l.adapter = LastFilesMaster.getAdapter(MainActivity.instance)
        adb.setView(l)
        return adb.create()
    }

    fun setCallback(callback: (String) -> Unit) {
        this.callback = callback
    }

}