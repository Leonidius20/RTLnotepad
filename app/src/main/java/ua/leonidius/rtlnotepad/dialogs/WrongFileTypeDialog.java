package ua.leonidius.rtlnotepad.dialogs;
import android.app.*;
import android.content.*;
import android.os.*;
import ua.leonidius.rtlnotepad.*;

public class WrongFileTypeDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private Activity activity;

	public WrongFileTypeDialog(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void onClick(DialogInterface p1, int id)
	{
		switch (id) {
			case Dialog.BUTTON_NEGATIVE:
				callback.callback(Callback.DONT_OPEN);
				break;
			case Dialog.BUTTON_POSITIVE:
				callback.callback(Callback.OPEN);
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setCancelable(false);
		adb.setMessage(R.string.wrong_file_type_warning);
		adb.setNegativeButton(R.string.no, this);
		adb.setPositiveButton(R.string.yes, this);
		return adb.create();
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public interface Callback {
		public static final byte OPEN = 0x00, DONT_OPEN = 0x01;

		public void callback(byte response);
	}
}
