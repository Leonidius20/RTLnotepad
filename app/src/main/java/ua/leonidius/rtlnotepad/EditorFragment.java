package ua.leonidius.rtlnotepad;
import android.app.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import ua.leonidius.rtlnotepad.dialogs.*;
import ua.leonidius.rtlnotepad.utils.*;

public class EditorFragment extends Fragment
{
	public String currentEncoding = "UTF-8";
	private EditText editor;
    private TestActivity mActivity;
	public boolean hasUnsavedChanges = false;
	
    public String mTag;
    public File file;
	
	public String textToPaste = null;
	
	TextWatcher textWatcher = null;
	
	// Reduant
	public EditorFragment() {
		super();
		mActivity = TestActivity.getInstance();
	}
	
	// Restoration
	public EditorFragment(String text) {
		super();
		mActivity = TestActivity.getInstance();
		textToPaste = text;
	}
	
	// Opening a file
	public EditorFragment(TestActivity activity, File file) {
		super();
		mActivity = activity;
        mTag = file.getName();
        
		this.file = file;
	}
	
	// New file
	public EditorFragment(TestActivity activity) {
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
		// Getting the text to paste (if needed)
		if (textToPaste == null && file != null) {
			try {
				textToPaste = FileWorker.read(file, currentEncoding);
			} catch (IOException e) {
				Toast.makeText(mActivity, R.string.reading_error, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		
		// Initializing views
		View scrollView = inflater.inflate(R.layout.main, container, false);
		editor = scrollView.findViewById(R.id.editor);
		editor.setTextSize(mActivity.pref.getInt(mActivity.PREF_TEXT_SIZE, mActivity.SIZE_MEDIUM));
		
		// Pasting the text
		if (textToPaste != null) editor.setText(textToPaste);
		
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
				ed.setCallback(new EncodingDialog.Callback() {
						@Override
						public void callback(String selectedEncoding)
						{
							setEncoding(selectedEncoding);
						}
					});
				ed.show(mActivity.getFragmentManager(), "encodingDialog");
				return true;
			case R.id.options_close:
				final ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();
				if (hasUnsavedChanges) {
					final EditorFragment fragment = this;
					CloseTabDialog ctd = new CloseTabDialog(mActivity);
					ctd.setCallback(new CloseTabDialog.Callback() {
							@Override
							public void callback(byte response)
							{
								if (response == DONT_SAVE) {
									mActivity.closeTab(selectedTab);
								} else if (response == SAVE) {
									if (file == null) {
										SaveDialog saveDialog = new SaveDialog(mActivity, fragment);
										saveDialog.setCallback(new SaveDialog.Callback() {
												@Override
												public void callback(File file)
												{
													LastFilesMaster.add(file);
													mActivity.closeTab(selectedTab);
												}
											});
										saveDialog.show(mActivity.getFragmentManager(), "saveDialog");
									} else {
										saveChanges();
										mActivity.closeTab(selectedTab);
									}
								}
							}
						});
					ctd.show(mActivity.getFragmentManager(), "closeTabDialog");
				} else {
					mActivity.closeTab(selectedTab);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void openSaveDialog() {
		SaveDialog saveDialog = new SaveDialog(mActivity, this);
		saveDialog.setCallback(new SaveDialog.Callback() {
				@Override
				public void callback(File newFile)
				{
					ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();
					file = newFile;
					selectedTab.setText(file.getName());
					setTextChanged(false);
					LastFilesMaster.add(file);
				}
			});
		saveDialog.show(mActivity.getFragmentManager(), "saveDialog");
	}
	
	private void saveChanges() {
		try {
			FileWorker.write(file, editor.getText().toString(), currentEncoding);
		} catch (Exception e) {
			Toast.makeText(mActivity, R.string.file_save_error, Toast.LENGTH_SHORT).show();
		}
		setTextChanged(false);
	}
	
	public EditText getEditor() {
		return editor;
	}
	
	public void setTextChanged(boolean changed) {
		if (hasUnsavedChanges == changed) return;
		hasUnsavedChanges = changed;
		ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();
		
		String name;
		if (file == null) name = getString(R.string.new_document);
		else name = file.getName();
		
		if (changed) selectedTab.setText(name+"*");
		else selectedTab.setText(name);
	}
	
	public void setEncoding(final String newEncoding) {
		if (file == null) currentEncoding = newEncoding;
		else {
			if (!hasUnsavedChanges) {
				try
				{
					editor.setText(FileWorker.read(file, newEncoding));
					currentEncoding = newEncoding;
					setTextChanged(false);
				}
				catch (Exception e)
				{
					Toast.makeText(mActivity, R.string.encoding_error, Toast.LENGTH_SHORT).show();
				}
			} else {
				ConfirmEncodingChangeDialog cecd = new ConfirmEncodingChangeDialog(mActivity);
				cecd.setCallback(new ConfirmEncodingChangeDialog.Callback() {

						@Override
						public void callback(byte response)
						{
							if (response == CHANGE) {
								try
								{
									editor.setText(FileWorker.read(file, newEncoding));
									currentEncoding = newEncoding;
									setTextChanged(false);
								}
								catch (Exception e)
								{
									Toast.makeText(mActivity, R.string.encoding_error, Toast.LENGTH_SHORT).show();
								}
							}
						}

					
				});
				cecd.show(mActivity.getFragmentManager(), "cofirmEncodingChangeDialog");
			}
		}
	}
	
}
