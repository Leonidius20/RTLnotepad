package ua.leonidius.rtlnotepad.dialogs;
import android.os.*;
import ua.leonidius.rtlnotepad.*;
import android.widget.*;
import android.view.*;
import java.io.*;
import ua.leonidius.rtlnotepad.utils.*;
import android.app.*;

public class LastFilesDialog extends DialogFragment implements AdapterView.OnItemClickListener
{
	Activity activity;
	Callback callback;
	
	public LastFilesDialog(Activity activity, Callback callback) {
		this.activity = activity;
		this.callback = callback;
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View item, int p3, long p4)
	{
		callback.callback(((TextView)item.findViewById(R.id.lastFilesItem_path)).getText().toString());
		getDialog().cancel();
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setTitle(R.string.last_files);
		ListView l = new ListView(activity);
		l.setOnItemClickListener(this);
		l.setAdapter(LastFilesMaster.getAdapter(activity));
		adb.setView(l);
		return adb.create();
	}
	
	public interface Callback {
		public void callback(String path);
	}
	
}
