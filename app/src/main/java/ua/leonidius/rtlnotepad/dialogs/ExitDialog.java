package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

public class ExitDialog extends DialogFragment implements AlertDialog.OnClickListener
{

	@Override
	public void onClick(DialogInterface p1, int id)
	{
		if (id == Dialog.BUTTON_POSITIVE) MainActivity.getInstance().finish();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.getInstance());
		adb.setCancelable(false);
		adb.setTitle(R.string.exit);
		adb.setMessage(R.string.unsaved_files);
		adb.setPositiveButton(R.string.yes, this);
		adb.setNegativeButton(R.string.no, this);
		return adb.create();
	}
	
}