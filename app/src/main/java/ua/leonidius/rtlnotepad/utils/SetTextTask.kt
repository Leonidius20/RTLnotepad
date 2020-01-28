package ua.leonidius.rtlnotepad.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog

class SetTextTask(private val context: Context,
                  private val fragMgr: FragmentManager? = null,
                  private val oldEditText: EditText? = null,
                  private val text: CharSequence,
                  private val callback: (EditText) -> Unit): AsyncTask<Unit, Unit, EditText>() {

    private lateinit var dialog: LoadingDialog

    override fun onPreExecute() {
        Log.d("SetTextTask", "inside onPreExecute()")
        fragMgr?.let {
            dialog = LoadingDialog()
            dialog.show(it, "loadingDialog")
            Log.d("SetTextTask", "dialog should've been shown by now")
        }
    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg params: Unit?): EditText {
        val editText = EditText(context)
        editText.setText(text)
        return editText
    }

    override fun onPostExecute(result: EditText?) {
        Log.d("SetTextTask", "Inside onPostExecute()")
        oldEditText?.let {
            val group = it.parent as ViewGroup
            val index = group.indexOfChild(it)
            val layoutParams = it.layoutParams
            group.removeView(it)
            group.addView(result, index, layoutParams)
        }
        callback(result!!)
        if (::dialog.isInitialized) dialog.dismiss()
    }

}