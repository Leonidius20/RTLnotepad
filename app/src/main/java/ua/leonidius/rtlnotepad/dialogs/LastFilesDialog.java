package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;
import ua.leonidius.rtlnotepad.utils.LastFilesMaster;

public class LastFilesDialog extends DialogFragment implements AdapterView.OnItemClickListener
{
	Callback callback;

	@Override
	public void onItemClick(AdapterView<?> p1, View item, int p3, long p4)
	{
		callback.callback(((TextView)item.findViewById(R.id.lastFilesItem_path)).getText().toString());
		getDialog().cancel();
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.getInstance());
		adb.setTitle(R.string.last_files);
		ListView l = new ListView(MainActivity.getInstance());
		l.setOnItemClickListener(this);
		l.setAdapter(LastFilesMaster.getAdapter(MainActivity.getInstance()));
		adb.setView(l);
		return adb.create();
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public interface Callback {
		void callback(String path);
	}
	
}