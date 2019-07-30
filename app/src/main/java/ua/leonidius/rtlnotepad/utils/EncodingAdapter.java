package ua.leonidius.rtlnotepad.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import ua.leonidius.rtlnotepad.R;

public class EncodingAdapter extends ArrayAdapter
{
	Object[] data;
	Context context;
	public String selectedEncoding;
	
	public EncodingAdapter(Context context, Object[] data, String currentEncoding) {
		super(context, R.layout.encoding_list_item, R.id.radio_text, data);
		this.context = context;
		this.data = data;
		this.selectedEncoding = currentEncoding;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup p3)
	{
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.encoding_list_item, null, false);
		}
		
		TextView tv = view.findViewById(R.id.radio_text);
		tv.setText((String)data[position]);
		
		RadioButton r = view.findViewById(R.id.radio_button);
		String encoding = (String)data[position];
		r.setChecked(encoding.equalsIgnoreCase(selectedEncoding));
		
		r.setTag(data[position]);
		
		r.setOnClickListener(clickedView -> {
			selectedEncoding = (String)clickedView.getTag();
			notifyDataSetChanged();
		});
		
		return view;
	}
	
}
