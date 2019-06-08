package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

import java.io.File;

public class OpenDialog extends NavigationDialog
{
	Callback callback;
	
	public OpenDialog(MainActivity activity, Callback callback) {
		super(activity);
		this.callback = callback;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setTitle(R.string.open);
		
		View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_open, null);
		initView(dialogView);
		adb.setView(dialogView);
		
		onCreateOpenDir(savedInstanceState);
		
		return adb.create();
	}
	
	@Override
	protected void onFileClick(File file)
	{
		if (!isText(file)) {
			WrongFileTypeDialog wftd = new WrongFileTypeDialog(activity, open -> {
				if (open) {
					callback.callback(file);
					getDialog().cancel();
				}
			});
			wftd.show(activity.getFragmentManager(), "wrongFileTypeDialog");
			return;
		}
		
		callback.callback(file);
		getDialog().cancel();
	}
	
	private boolean isText(File file) {
		try {
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())).split("/")[0].equals("text");
		} catch (Exception e) {return false;}
	}
	
	public interface Callback {
		void callback(File file);
	}
	
}