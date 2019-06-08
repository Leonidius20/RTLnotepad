package ua.leonidius.rtlnotepad;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import ua.leonidius.rtlnotepad.dialogs.CloseTabDialog;
import ua.leonidius.rtlnotepad.dialogs.ConfirmEncodingChangeDialog;
import ua.leonidius.rtlnotepad.dialogs.EncodingDialog;
import ua.leonidius.rtlnotepad.dialogs.SaveDialog;
import ua.leonidius.rtlnotepad.utils.FileWorker;
import ua.leonidius.rtlnotepad.utils.LastFilesMaster;

import java.io.File;

public class EditorFragment extends Fragment
{
	public String currentEncoding = "UTF-8";
	private EditText editor;
    private MainActivity mActivity;
	public boolean hasUnsavedChanges = false;
	
    public String mTag;
    public File file;
	
	private String textToPaste = null;
	
	private TextWatcher textWatcher = null;
	
	// Redundant
	public EditorFragment() {
		super();
		mActivity = MainActivity.getInstance();
	}
	
	// Restoration
	public EditorFragment(String text) {
		super();
		mActivity = MainActivity.getInstance();
		textToPaste = text;
	}
	
	// Opening a file
	public EditorFragment(MainActivity activity, File file) {
		super();
		mActivity = activity;
        mTag = file.getName();
        
		this.file = file;
	}
	
	// New file
	public EditorFragment(MainActivity activity) {
		super();
		mActivity = activity;
        mTag = String.valueOf(System.currentTimeMillis()); // Current date
		file = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState)
	{
		// Initializing views
		View scrollView = inflater.inflate(R.layout.main, container, false);
		editor = scrollView.findViewById(R.id.editor);
		editor.setTextSize(mActivity.pref.getInt(mActivity.PREF_TEXT_SIZE, mActivity.SIZE_MEDIUM));
		
		// Pasting the text
		if (textToPaste != null) editor.setText(textToPaste);
		else if (file != null) readAndSetText(file, currentEncoding, success -> {
			if (!success) close(); // Close if failed to read requested file
		});
		
		textWatcher = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4){}
				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4){}
				@Override
				public void afterTextChanged(Editable p1)
				{
					setTextChanged(true);
				}
			};
		editor.addTextChangedListener(textWatcher);
		
		return scrollView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.editor_options, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.options_save:
				if (file == null) openSaveDialog();
				else saveChanges();
				return true;
			case R.id.options_save_as:
				openSaveDialog();
				return true;
			case R.id.options_encoding:
				EncodingDialog ed = new EncodingDialog(mActivity, this);
				ed.setCallback(this::setEncoding);
				ed.show(mActivity.getFragmentManager(), "encodingDialog");
				return true;
			case R.id.options_close:
				close();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Shows a SaveDialog and writes the text to the selected file.
	 */
	private void openSaveDialog() {
		SaveDialog saveDialog = new SaveDialog(currentEncoding, (newFile, newEncoding) -> {
			writeFile(newFile, editor.getText().toString(), newEncoding, ((savedFile, usedEncoding) -> {
				this.file = savedFile;
				this.currentEncoding = usedEncoding;
				setTextChanged(false);
				LastFilesMaster.add(savedFile);
			}));
		});
		saveDialog.show(mActivity.getFragmentManager(), "saveDialog");
	}

	/**
	 * Saves changes in the current file. Doesn't check if the file equals null.
	 */
	private void saveChanges() {
		writeFile(file, editor.getText().toString(), currentEncoding, ((file1, encoding) -> setTextChanged(false)));
	}

	/**
	 * Writes a file to the disk using FileWorker.WriteTask
	 * @param file File to write into
	 * @param text Text to write into the file
	 * @param encoding Encoding to use
	 * @param callback Defines what to do after the writing. Only called if writing was successful.
	 */
	private void writeFile (File file, String text, String encoding, WriteCallback callback) {
		FileWorker.WriteTask task = new FileWorker.WriteTask(file, text, encoding, success -> {
			if (success) {
				Toast.makeText(mActivity, R.string.done, Toast.LENGTH_SHORT).show();
				callback.call(file, encoding);
			} else Toast.makeText(mActivity, R.string.file_save_error, Toast.LENGTH_SHORT).show();
		});
		task.execute();
	}

	interface WriteCallback {
		void call(File file, String encoding);
	}
	
	public EditText getEditor() {
		return editor;
	}
	
	private void setTextChanged(boolean changed) {
		if (hasUnsavedChanges == changed) return;
		hasUnsavedChanges = changed;
		ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();
		
		String name;
		if (file == null) name = getString(R.string.new_document);
		else name = file.getName();

		if (changed) selectedTab.setText(name+"*");
		else selectedTab.setText(name);
	}
	
	public void setEncoding(String newEncoding) {
		if (file == null) {
			currentEncoding = newEncoding;
			return;
		}

		if (!hasUnsavedChanges) {
			readAndSetText(file, newEncoding, success -> {if (success) currentEncoding = newEncoding;});
			return;
		}

		ConfirmEncodingChangeDialog cecd = new ConfirmEncodingChangeDialog(mActivity);
		cecd.setCallback(change -> {
			if (change) readAndSetText(file, newEncoding, success -> {if (success) currentEncoding = newEncoding;});
		});
		cecd.show(mActivity.getFragmentManager(), "confirmEncodingChangeDialog");
	}

	/**
	 * Reads a specified file and sets its contents as a text to the editor.
	 * Shows a Toast if the reading fails.
	 * @param file File to read
	 * @param encoding Encoding to use for decoding of the file
	 */
	private void readAndSetText (File file, String encoding, ReadCallback callback) {
		// TODO: preserve the task when rotated
		FileWorker.ReadTask task = new FileWorker.ReadTask(file, encoding, result -> {
			if (result == null) {
				Toast.makeText(mActivity, R.string.reading_error, Toast.LENGTH_SHORT).show();
				callback.call(false);
			} else {
				editor.setText(result); // Takes a lot of time for big texts. Most of it
				setTextChanged(false);
				callback.call(true);
			}
		});
		task.execute();
	}

	interface ReadCallback {
		void call(boolean success);
	}

	/**
	 * Closes the tab to which the EditorFragment is assigned.
	 * Provides an opportunity to save changes if they are not saved.
	 */
	private void close() {
		final ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();

		if (!hasUnsavedChanges) {
			mActivity.closeTab(selectedTab);
			return;
		}

		CloseTabDialog ctd = new CloseTabDialog(mActivity);
		ctd.setCallback(save -> {
			if (!save) {
				mActivity.closeTab(selectedTab);
				return;
			}

			if (file != null) {
				saveChanges();
				mActivity.closeTab(selectedTab);
				return;
			}

			SaveDialog saveDialog = new SaveDialog(currentEncoding, (selectedFile, selectedEncoding) -> {
				writeFile(selectedFile, editor.getText().toString(), selectedEncoding, (savedFile, usedEncoding) -> {
					LastFilesMaster.add(savedFile);
					mActivity.closeTab(selectedTab);
				});
			});
			saveDialog.show(mActivity.getFragmentManager(), "saveDialog");
		});
		ctd.show(mActivity.getFragmentManager(), "closeTabDialog");
	}
	
}