package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveDialog extends NavigationDialog implements AlertDialog.OnClickListener
{
	private EditText nameField;
	private Spinner encodingSpinner;
	private Callback callback;
	private String currentEncoding;
	private Object[] availableEncodings;

	public SaveDialog (String currentEncoding, Callback callback) {
		super(MainActivity.getInstance());
		this.currentEncoding = currentEncoding;
		this.callback = callback;
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
		int currentEncodingPosition = 0;
		ArrayList <Map<String, Object>> data = new ArrayList<>();
		Map <String, Object> m;

		availableEncodings = Charset.availableCharsets().keySet().toArray();

		for (int i = 0; i < availableEncodings.length; i++) {
			String encoding = availableEncodings[i].toString();
			m = new HashMap<>();
			m.put("name", encoding);
			if (encoding.equalsIgnoreCase(currentEncoding)) {
				currentEncodingPosition = i;
			}
			data.add(m);
		}
		String[] from = {"name"};
		int[] to = {android.R.id.text1};

		// TODO: there is EncodingAdapter in AdapterFactory, maybe use it instead?
		encodingSpinner.setAdapter(new SimpleAdapter(activity, data, android.R.layout.simple_list_item_1, from, to));
		encodingSpinner.setSelection(currentEncodingPosition);
		
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
		final String encoding = availableEncodings[encodingSpinner.getSelectedItemPosition()].toString();
		
		final File openDir = currentDir; // Saving currentDir for re-opening the dialog
		final String nameFieldContents = nameField.getText().toString(); // Saving chosen file name
		
		if (file.exists()) {
			RewriteDialog rewriteDialog = new RewriteDialog(activity, rewrite -> {
				if (rewrite) callback.call(file, encoding);
				else {
					// Re-opening saveDialog
					// Adding currentDir and name field content as args
					// TODO: figure out a better way
					Bundle args = new Bundle();
					args.putSerializable("currentDir", openDir);
					args.putString("fileName", nameFieldContents);
					setArguments(args);
					show(activity.getFragmentManager(), "saveDialog");
				}
			});
			rewriteDialog.show(activity.getFragmentManager(), "rewriteDialog"); // try to show rewriteDialog with Frag's own manager
		} else callback.call(file, encoding);
	}
	
	public interface Callback {
		/**
		 * Gets called if a user has selected a file to write the text into and an encoding.
		 * Doesn't get called if the user dismisses the dialog.
		 * @param file File to which the user decided to write the text
		 * @param encoding Encoding to use to write the file
		 */
		void call(File file, String encoding);
	}

}