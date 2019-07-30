package ua.leonidius.rtlnotepad;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import ua.leonidius.navdialogs.SaveDialog;
import ua.leonidius.navdialogs.SaveDialogViewModel;
import ua.leonidius.rtlnotepad.dialogs.CloseTabDialog;
import ua.leonidius.rtlnotepad.dialogs.ConfirmEncodingChangeDialog;
import ua.leonidius.rtlnotepad.dialogs.EncodingDialog;
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog;
import ua.leonidius.rtlnotepad.utils.FileWorker;
import ua.leonidius.rtlnotepad.utils.LastFilesMaster;

import java.io.File;

public class EditorFragment extends Fragment {
    private String currentEncoding = "UTF-8";
    private EditText editor;
    private MainActivity mActivity;
    boolean hasUnsavedChanges = false;

    String mTag;
    public File file = null;

    //private String textToPaste = null;

    static final String ARGUMENT_FILE_PATH = "filePath";
    private static final String BUNDLE_FILE = "file",
            BUNDLE_TAG = "tag", BUNDLE_CURRENT_ENCODING = "currentEncoding",
            BUNDLE_HAS_UNSAVED_CHANGES = "hasUnsavedChanges";

    private boolean initialized = false;

    // Doesn't get called if fragment is recreated (setRetainInstance(true))
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setHasOptionsMenu(true);
        mActivity = (MainActivity) getActivity();

		/*if (savedState != null) {
			// Restoration
			file = (File)savedState.getSerializable(BUNDLE_FILE);
			mTag = savedState.getString(BUNDLE_TAG, getTag());
			currentEncoding = savedState.getString(BUNDLE_CURRENT_ENCODING, "UTF-8");
			hasUnsavedChanges = savedState.getBoolean(BUNDLE_HAS_UNSAVED_CHANGES, false);
			initialized = true;
		}*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        // Initializing views
        View scrollView = inflater.inflate(R.layout.main, container, false);
        editor = scrollView.findViewById(R.id.editor);
        editor.setTextSize(mActivity.pref.getInt(mActivity.PREF_TEXT_SIZE, mActivity.SIZE_MEDIUM));
        editor.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
            }

            @Override
            public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
            }

            @Override
            public void afterTextChanged(Editable p1) {
                setTextChanged(true);
            }

        });

        return scrollView;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (!initialized) { // Cold start
            mTag = String.valueOf(System.currentTimeMillis());
            currentEncoding = "UTF-8";

            Bundle arguments = getArguments();
            if (arguments != null) {
                String filePath = arguments.getString(ARGUMENT_FILE_PATH, null);
                if (filePath != null) file = new File(filePath);

                if (file != null) FileWorker.readFile(file, currentEncoding, (success, text) -> {
                    if (!success) close(); // Close if failed to read requested file
                    else setTextWithProgressDialog(text);
                });
            }
            initialized = true;
        } else {
            // getting the reference to the new activity, that was created after configuration change
            mActivity = (MainActivity) getActivity();
        }
    }

	/*@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (file != null) outState.putSerializable(BUNDLE_FILE, file);
		outState.putString(BUNDLE_TAG, mTag);
		outState.putString(BUNDLE_CURRENT_ENCODING, currentEncoding);
		outState.putBoolean(BUNDLE_HAS_UNSAVED_CHANGES, hasUnsavedChanges);
	}*/

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.editor_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_save:
                if (file == null) openSaveDialog();
                else saveChanges();
                return true;
            case R.id.options_save_as:
                openSaveDialog();
                return true;
            case R.id.options_encoding:
                EncodingDialog ed = new EncodingDialog();
                Bundle arguments = new Bundle();
                arguments.putString(EncodingDialog.ARGS_ENCODING, currentEncoding);
                ed.setArguments(arguments);
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
        SaveDialogViewModel model = ViewModelProviders.of(this).get(SaveDialogViewModel.class);
        model.getFile().observe(this, data -> {
            FileWorker.writeFile(data.first, editor.getText().toString(), data.second, success -> {
                if (success) {
                    file = data.first;
                    currentEncoding = data.second;
                    setTextChanged(false);
                    String successMessage = getResources().getString(R.string.file_save_success, data.first.getName());
                    Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
                    LastFilesMaster.add(data.first);
                } else {
                    Toast.makeText(getActivity(), R.string.file_save_error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        SaveDialog saveDialog = new SaveDialog();
        Bundle arguments = new Bundle();
        arguments.putString(SaveDialog.BUNDLE_CURRENT_ENCODING, currentEncoding);
        arguments.putString(SaveDialog.BUNDLE_FILE_NAME, ".txt");
        saveDialog.setArguments(arguments);
        saveDialog.show(getChildFragmentManager(), "saveDialog");
    }

    /**
     * Saves changes in the current file. Doesn't check if the file equals null.
     */
    private void saveChanges() {
        //writeFile(file, editor.getText().toString(), currentEncoding, ((file1, encoding) -> setTextChanged(false)));
        FileWorker.writeFile(file, editor.getText().toString(), currentEncoding, success -> {
            if (success) {
                setTextChanged(false);
                String successMessage = getResources().getString(R.string.file_save_success, file.getName());
                Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
            } else Toast.makeText(getActivity(), R.string.file_save_error, Toast.LENGTH_SHORT).show();
        });
    }

    public EditText getEditor() {
        return editor;
    }

    private void setTextChanged(boolean changed) {
        hasUnsavedChanges = changed;
        ActionBar.Tab selectedTab = mActivity.getActionBar().getSelectedTab();

        String name;
        if (file == null) name = getString(R.string.new_document);
        else name = file.getName();

        if (changed) selectedTab.setText(name + "*");
        else selectedTab.setText(name);
    }

    public void setEncoding(String newEncoding) {
        if (file == null) {
            currentEncoding = newEncoding;
            return;
        }

        if (!hasUnsavedChanges) {
            FileWorker.readFile(file, newEncoding, ((success, result) -> {
                if (success) {
                    getEditor().setText(result);
                    currentEncoding = newEncoding;
                } else {
                    Toast.makeText(getActivity(), R.string.reading_error, Toast.LENGTH_SHORT).show();
                }
            }));
            return;
        }

        ConfirmEncodingChangeDialog cecd = new ConfirmEncodingChangeDialog();
        cecd.setCallback(change -> {
            if (change) {
                FileWorker.readFile(file, newEncoding, ((success, result) -> {
                    if (success) {
                        getEditor().setText(result);
                        currentEncoding = newEncoding;
                    } else {
                        Toast.makeText(getActivity(), R.string.reading_error, Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
        cecd.show(mActivity.getFragmentManager(), "confirmEncodingChangeDialog");
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

        CloseTabDialog ctd = new CloseTabDialog();
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

            SaveDialogViewModel model = ViewModelProviders.of(this).get(SaveDialogViewModel.class);
            model.getFile().observe(this, data -> {
                FileWorker.writeFile(data.first, editor.getText().toString(), data.second, success -> {
                    if (success) {
                        String successMessage = getResources().getString(R.string.file_save_success, data.first.getName());
                        Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
                        LastFilesMaster.add(data.first);
                        mActivity.closeTab(selectedTab);
                    } else {
                        Toast.makeText(getActivity(), R.string.file_save_error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
            SaveDialog saveDialog = new SaveDialog();
            Bundle arguments = new Bundle();
            arguments.putString(SaveDialog.BUNDLE_CURRENT_ENCODING, currentEncoding);
            arguments.putString(SaveDialog.BUNDLE_FILE_NAME, ".txt");
            saveDialog.show(getChildFragmentManager(), "saveDialog");
        });
        ctd.show(mActivity.getFragmentManager(), "closeTabDialog");
    }

    /**
     * Sets a specified text to editor and shows a progress dialog while it is being set.
     * @param text Text to set
     */
    private void setTextWithProgressDialog(CharSequence text) {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getChildFragmentManager(), "loadingDialog");
        editor.setText(text);
        dialog.dismiss();
    }

}