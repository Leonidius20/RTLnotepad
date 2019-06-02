package ua.leonidius.rtlnotepad.dialogs;

import android.os.*;
import android.widget.*;
import java.io.*;
import ua.leonidius.rtlnotepad.*;
import android.view.*;
import android.content.*;
import ua.leonidius.rtlnotepad.utils.*;
import android.app.*;
import java.nio.charset.*;
import java.util.*;

public class SaveDialog extends NavigationDialog implements AlertDialog.OnClickListener
{
	private EditText nameField;
	private Spinner encodingSpinner;
	private Callback callback;
	private EditorFragment fragment;
	
	public SaveDialog(TestActivity activity, EditorFragment fragment) {
		super(activity);
		this.fragment = fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setTitle(R.string.save_as);
		adb.setPositiveButton(R.string.ok, this);
		
		View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_save_as, null);
		initView(dialogView);
		
		nameField = dialogView.findViewById(R.id.saveasdialog_name);
		
		// Setting up Spinner with encodings
		encodingSpinner = dialogView.findViewById(R.id.saveasdialog_encoding_spinner);
		int currentEncondingPosition = 0;
		ArrayList <Map<String, Object>> data = new ArrayList <Map<String, Object>>();
		Map <String, Object> m;
		Object[] encodings = Charset.availableCharsets().keySet().toArray();
		for (int i = 0; i < encodings.length; i++) {
			m = new HashMap<String, Object>();
			m.put("name", (String)encodings[i]);
			String encoding = (String)encodings[i];
			if (encoding.equalsIgnoreCase(fragment.currentEncoding)) {
				currentEncondingPosition = i;
			}
			data.add(m);
		}
		String[] from = {"name"};
		int[] to = {android.R.id.text1};
		
		encodingSpinner.setAdapter(new SimpleAdapter(activity, data, android.R.layout.simple_list_item_1, from, to));
		encodingSpinner.setSelection(currentEncondingPosition);
		
		adb.setView(dialogView);
		
		if (getArguments() == null) {
			onCreateOpenDir(savedInstanceState);
		} else {
			openDir((File)getArguments().getSerializable("currentDir"));
			nameField.setText(getArguments().getString("fileName"));
		}
		
		return adb.create();
	}

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		final File file = new File(currentDir.getPath()+"/"+nameField.getText());
		final String encoding = ((TextView)encodingSpinner.getSelectedView()).getText().toString();
		final String text = fragment.getEditor().getText().toString();
		
		final File openDir = currentDir; // Saving currentDir for re-opening the dialog
		final String nameFieldContents = nameField.getText().toString(); // Saving chosen file name
		
		if (file.exists()) {
			RewriteDialog rewriteDialog = new RewriteDialog(activity);
			rewriteDialog.setCallback(new RewriteDialog.Callback() {
					@Override
					public void callback(byte response)
					{
						switch (response) {
							case RewriteDialog.Callback.REWRITE:
								try
								{
									FileWorker.write(file, text, encoding);
									callback.callback(file);
								}
								catch (IOException e)
								{
									Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT);
								}
								break;
							case RewriteDialog.Callback.DONT_REWRITE:
								// Re-opening saveDialog
								// Adding currentDir and name field content as args
								Bundle args = new Bundle();
								args.putSerializable("currentDir", openDir);
								args.putString("fileName", nameFieldContents);
								setArguments(args);
								show(activity.getFragmentManager(), "saveDialog");
								//openDir(openDir);
								//nameField.setText(nameFieldContents);
								break;
						}
					}
			});
			rewriteDialog.show(activity.getFragmentManager(), "rewriteDialog");
			return;
		}
		
		// Writing
		//save(file, encoding, exitAfterSave, openAfterSave);
		try
		{
			FileWorker.write(file, text, encoding);
		}
		catch (IOException e)
		{
			Toast.makeText(activity, R.string.file_save_error, Toast.LENGTH_SHORT);
		}

		callback.callback(file);
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public interface Callback {
		void callback(File file);
	}

}
