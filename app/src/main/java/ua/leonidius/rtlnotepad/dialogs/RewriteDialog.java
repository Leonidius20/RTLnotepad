package ua.leonidius.rtlnotepad.dialogs;

import android.os.*;
import android.content.*;
import ua.leonidius.rtlnotepad.*;
import java.io.*;
import android.app.*;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private Activity activity;
	
	public RewriteDialog(Activity activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public void onClick(DialogInterface p1, int id)
	{
		switch (id) {
			case Dialog.BUTTON_NEGATIVE:
				callback.callback(Callback.DONT_REWRITE);
				break;
			case Dialog.BUTTON_POSITIVE:
				callback.callback(Callback.REWRITE);
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setCancelable(false);
		adb.setMessage(R.string.file_overwrite);
		adb.setNegativeButton(R.string.no, this);
		adb.setPositiveButton(R.string.yes, this);
		return adb.create();
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public interface Callback {
		public static final byte REWRITE = 0x00, DONT_REWRITE = 0x01;

		public void callback(byte response);
	}
	
}
