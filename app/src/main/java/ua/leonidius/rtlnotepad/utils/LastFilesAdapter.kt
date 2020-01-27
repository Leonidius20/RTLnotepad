package ua.leonidius.rtlnotepad.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import ua.leonidius.rtlnotepad.R

class LastFilesAdapter(private val context: Context): BaseAdapter() {

    private val uriList = LastFilesMaster.getLastFiles().toList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = convertView ?:
        LayoutInflater.from(context).inflate(R.layout.last_files_item, parent, false)

        val uri : Uri = Uri.parse(uriList[position])

        view.findViewById<TextView>(R.id.lastFilesItem_name).text = getFileName(context, uri)
        view.findViewById<TextView>(R.id.lastFilesItem_path).text = uri.path ?: ""
        view.findViewById<ImageView>(R.id.lastFilesItem_image).setImageResource(R.drawable.file)
        view.tag = uri

        return view
    }

    override fun getItem(position: Int): Any {
        return uriList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return uriList.size
    }

}