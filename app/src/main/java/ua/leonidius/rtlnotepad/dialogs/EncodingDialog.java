package ua.leonidius.rtlnotepad.dialogs;
import android.app.*;
import android.os.*;
import ua.leonidius.rtlnotepad.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.view.*;
import ua.leonidius.rtlnotepad.utils.*;
import java.io.*;
import java.nio.charset.*;
import android.content.*;

public class EncodingDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private EditorFragment fragment;
	private Activity context;
	private EncodingAdapter adapter;

	public EncodingDialog(Activity context, EditorFragment fragment) {
		super();
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		adb.setTitle(R.string.encoding);
		adb.setPositiveButton(R.string.apply, this);
		adb.setNegativeButton(R.string.cancel, this);
		
		ListView listView = new ListView(context);
		adapter = new EncodingAdapter(context, Charset.availableCharsets().keySet().toArray(), fragment.currentEncoding);
		listView.setAdapter(adapter);
		adb.setView(listView);
		
		return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface p1, int id)
	{
		if (id == Dialog.BUTTON_POSITIVE) {
			callback.callback(adapter.selectedEncoding);
		}
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public interface Callback {
		public void callback(String selectedEncoding);
	}
	
}
