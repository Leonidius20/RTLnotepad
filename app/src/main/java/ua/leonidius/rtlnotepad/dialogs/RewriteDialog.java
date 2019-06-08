package ua.leonidius.rtlnotepad.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ua.leonidius.rtlnotepad.R;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private Activity activity;
	
	public RewriteDialog(Activity activity, Callback callback) {
		super();
		this.activity = activity;
		this.callback = callback;
	}
	
	@Override
	public void onClick(DialogInterface p1, int id)
	{
		switch (id) {
			case Dialog.BUTTON_NEGATIVE:
				callback.callback(false);
				break;
			case Dialog.BUTTON_POSITIVE:
				callback.callback(true);
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
	
	public interface Callback {
		void callback(boolean rewrite);
	}
	
}