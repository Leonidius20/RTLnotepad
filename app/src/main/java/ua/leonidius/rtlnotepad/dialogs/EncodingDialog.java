package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListView;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;
import ua.leonidius.rtlnotepad.utils.EncodingAdapter;

import java.nio.charset.Charset;

public class EncodingDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private EncodingAdapter adapter;

	public static final String ARGS_ENCODING = "currentEncoding";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		String currentEncoding = getArguments().getString(ARGS_ENCODING, "UTF-8");
		// TODO: check if that works

		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.getInstance());
		adb.setTitle(R.string.encoding);
		adb.setPositiveButton(R.string.apply, this);
		adb.setNegativeButton(R.string.cancel, this);
		
		ListView listView = new ListView(MainActivity.getInstance());
		adapter = new EncodingAdapter(MainActivity.getInstance(), Charset.availableCharsets().keySet().toArray(), currentEncoding);
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
		void callback(String selectedEncoding);
	}
	
}