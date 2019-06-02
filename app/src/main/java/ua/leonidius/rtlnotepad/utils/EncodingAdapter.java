package ua.leonidius.rtlnotepad.utils;
import android.widget.*;
import android.view.*;
import android.content.*;
import ua.leonidius.rtlnotepad.*;

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
			view = inflater.inflate(R.layout.encoding_list_item, null);
		}
		
		TextView tv = view.findViewById(R.id.radio_text);
		tv.setText((String)data[position]);
		
		RadioButton r = view.findViewById(R.id.radio_button);
		String encoding = (String)data[position];
		r.setChecked(encoding.equalsIgnoreCase(selectedEncoding));
		
		r.setTag((String)data[position]);
		
		r.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectedEncoding = (String)view.getTag();
				notifyDataSetChanged();
			}
		});
		
		return view;
	}
	
}
