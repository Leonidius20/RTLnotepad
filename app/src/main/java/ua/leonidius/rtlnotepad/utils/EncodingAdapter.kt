package ua.leonidius.rtlnotepad.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import ua.leonidius.rtlnotepad.R

class EncodingAdapter(internal var context: Context, private var data: Array<String>, var selectedEncoding: String) : ArrayAdapter<String>(context, R.layout.encoding_list_item, R.id.radio_text, data) {

    override fun getView(position: Int, convertView: View?, p3: ViewGroup): View {
        var view = convertView

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.encoding_list_item, null, false)
        }

        val tv = view!!.findViewById<TextView>(R.id.radio_text)
        tv.text = data[position]

        val r = view.findViewById<RadioButton>(R.id.radio_button)
        val encoding = data[position]
        r.isChecked = encoding.equals(selectedEncoding, ignoreCase = true)

        r.tag = data[position]

        r.setOnClickListener { clickedView ->
            selectedEncoding = clickedView.tag as String
            notifyDataSetChanged()
        }

        return view
    }

}
