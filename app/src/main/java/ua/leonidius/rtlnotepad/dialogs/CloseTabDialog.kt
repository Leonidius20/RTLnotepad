package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

/**
	This dialog is showed when user tries to close a tab with unsaved changes.
**/

public class CloseTabDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.getInstance());
		adb.setMessage(R.string.close_tab_warning);
		adb.setNegativeButton(R.string.no, this);
		adb.setPositiveButton(R.string.yes, this);
		adb.setNeutralButton(R.string.cancel, this);
		return adb.create();
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
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public interface Callback {
		void callback(boolean save);
	}

}