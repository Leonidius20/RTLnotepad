package ua.leonidius.rtlnotepad.dialogs;
import android.app.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import java.io.*;
import ua.leonidius.rtlnotepad.*;

public class OpenDialog extends NavigationDialog
{
	Callback callback;
	
	public OpenDialog(TestActivity activity) {
		super(activity);
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
	protected void onFileClick(final File file)
	{
		if (!isText(file)) {
			WrongFileTypeDialog wftd = new WrongFileTypeDialog(activity);
			wftd.setCallback(new WrongFileTypeDialog.Callback() {
					@Override
					public void callback(byte response)
					{
						if (response == OPEN) {
							callback.callback(file);
							getDialog().cancel();
						}
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
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public interface Callback
	{
		void callback(File file);
	}
	
}
