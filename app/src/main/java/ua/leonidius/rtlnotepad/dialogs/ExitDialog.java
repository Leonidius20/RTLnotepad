package ua.leonidius.rtlnotepad.dialogs;

import android.os.*;
import ua.leonidius.rtlnotepad.*;
import android.content.*;
import android.app.*;

public class ExitDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	TestActivity activity;
	
	public ExitDialog(TestActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void onClick(DialogInterface p1, int id)
	{
		if (id == Dialog.BUTTON_POSITIVE) activity.finish();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setCancelable(false);
		adb.setTitle(R.string.exit);
		adb.setMessage(R.string.unsaved_files);
		adb.setPositiveButton(R.string.yes, this);
		adb.setNegativeButton(R.string.no, this);
		return adb.create();
	}
	
}
