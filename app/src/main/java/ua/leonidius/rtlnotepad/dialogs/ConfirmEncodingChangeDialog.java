package ua.leonidius.rtlnotepad.dialogs;
import android.app.*;
import android.content.*;
import android.os.*;
import ua.leonidius.rtlnotepad.*;

public class ConfirmEncodingChangeDialog extends DialogFragment implements AlertDialog.OnClickListener
{
	private Callback callback;
	private Activity activity;

	public ConfirmEncodingChangeDialog(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setMessage(R.string.encoding_change_warning);
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
				callback.callback(Callback.DONT_CHANGE);
				break;
			case Dialog.BUTTON_POSITIVE:
				callback.callback(Callback.CHANGE);
				break;
		}
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public interface Callback {
		public static final byte CHANGE = 0x00, DONT_CHANGE = 0x01;

		public void callback(byte response);
	}

}
