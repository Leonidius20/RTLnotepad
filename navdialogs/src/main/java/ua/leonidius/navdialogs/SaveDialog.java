package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveDialog extends NavigationDialog implements AlertDialog.OnClickListener {

    private EditText nameField;
    private Spinner encodingSpinner;
    private Model viewModel = null;
    //private String currentEncoding;
    //private String[] availableEncodings;

    //public static final String BUNDLE_CURRENT_ENCODING = "currentEncoding";
    //public static final String BUNDLE_FILE_NAME = "fileName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            currentEncoding = getArguments().getString(BUNDLE_CURRENT_ENCODING, "UTF-8");
        } else currentEncoding = "UTF-8";*/
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.save_as);
        adb.setPositiveButton(android.R.string.ok, this);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.navdialogs_dialog_save_as, null, false);
        initView(dialogView, savedInstanceState);
        adb.setView(dialogView);

        openDir(getViewModel().currentDir);

        Dialog dialog = adb.create();
        setMaxHeight(dialog);
        return dialog;
    }

    @Override
    protected void initView(View dialogView, Bundle savedState) {
        super.initView(dialogView, savedState);

        nameField = dialogView.findViewById(R.id.saveasdialog_name);
        String fileName;
        /*if (getArguments() != null && savedState == null) {
            fileName = getArguments().getString(BUNDLE_FILE_NAME, "");
            nameField.setText(fileName);
        }*/
        nameField.setText(getViewModel().fileName);

        encodingSpinner = dialogView.findViewById(R.id.saveasdialog_encoding_spinner);

        String[] availableEncodings = getViewModel().getAvailableEncodings();

        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> m;
        int currentEncodingPosition = 0;
        for (int i = 0; i < availableEncodings.length; i++) {
            String encoding = availableEncodings[i];
            m = new HashMap<>();
            m.put("name", encoding);
            if (encoding.equalsIgnoreCase(getViewModel().currentEncoding)) {
                currentEncodingPosition = i;
            }
            data.add(m);
        }
        String[] from = {"name"};
        int[] to = {android.R.id.text1};

        encodingSpinner.setAdapter(new SimpleAdapter(getActivity(), data, android.R.layout.simple_list_item_1, from, to));
        encodingSpinner.setSelection(currentEncodingPosition);
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        File file = new File(getViewModel().currentDir.getPath() + File.separatorChar + nameField.getText());
        String encoding = getViewModel().getAvailableEncodings()[encodingSpinner.getSelectedItemPosition()];

        if (!file.exists()) {
            getViewModel().callback.call(file, encoding);
            return;
        }

        RewriteDialog.create(this, rewrite -> {
            if (rewrite) getViewModel().callback.call(file, encoding);
            else show(getFragmentManager(), "saveDialog");
        }).show(getFragmentManager(), "rewriteDialog");

        /*RewriteDialog.Model model;
        if (getParentFragment() != null) {
             model = ViewModelProviders.of(getParentFragment()).get(RewriteDialog.Model.class);
        } else if (getActivity() != null) {
            model = ViewModelProviders.of(getActivity()).get(RewriteDialog.Model.class);
        } else {
            Log.e("NavDialogs", "SaveDialog doesn't have a parent!");
            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
            return;
        }
        model.rewrite().observe(this, rewrite -> {
            if (rewrite) {
                getViewModel().callback.call(file, encoding);
            } else {
                /*Bundle args = new Bundle();
                args.putSerializable(BUNDLE_CURRENT_DIR, currentDir);
                args.putString(BUNDLE_FILE_NAME, nameField.getText().toString());
                setArguments(args);*/
                /*show(getFragmentManager(), "saveDialog");
            }
        });

        RewriteDialog rewriteDialog = new RewriteDialog();
        rewriteDialog.show(getFragmentManager(), "rewriteDialog");*/

    }

    /*private void callback(File file, String encoding) {
        Model model;
        Log.d("NavDialogs", "getActivity() is null: " + (getActivity() == null)
                + ", getParentFragment() is null: " + (getParentFragment() == null));
        if (getParentFragment() != null) {
            model = ViewModelProviders.of(getParentFragment()).get(Model.class);
        } else if (getActivity() != null) {
            model = ViewModelProviders.of(getActivity()).get(Model.class);
        } else {
            Log.e("NavDialogs", "SaveDialog doesn't have a parent!");
            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
            return;
        }
        model.setData(file, encoding);
    }*/

    public static SaveDialog create(Fragment frag, String defaultName, String defaultEncoding, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultName, defaultEncoding, Environment.getExternalStorageDirectory(), callback);
    }

    public static SaveDialog create(FragmentActivity frag, String defaultName, String defaultEncoding, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultName, defaultEncoding, Environment.getExternalStorageDirectory(), callback);
    }

    public static SaveDialog create(Fragment frag, String defaultName, String defaultEncoding,
                                    File defaultDir, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultName, defaultEncoding, defaultDir, callback);
    }

    public static SaveDialog create(FragmentActivity frag, String defaultName, String defaultEncoding,
                                    File defaultDir, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultName, defaultEncoding, defaultDir, callback);
    }

    private static SaveDialog create(Model model, String defaultName, String defaultEncoding, File defaultDirectory, Callback callback) {
        SaveDialog dialog = new SaveDialog();
        dialog.viewModel = model;
        Model viewModel = dialog.getViewModel();
        viewModel.currentEncoding = defaultEncoding;
        viewModel.fileName = defaultName;
        viewModel.currentDir = defaultDirectory;
        viewModel.callback = callback;
        return dialog;
    }

    // TODO: rework
    @Override
    public Model getViewModel() {
        if (viewModel == null) {
            Log.d("NavDialogs", "getActivity() is null: " + (getActivity() == null)
                    + ", getParentFragment() is null: " + (getParentFragment() == null));
            if (getParentFragment() != null) {
                viewModel = ViewModelProviders.of(getParentFragment()).get(Model.class);
            } else if (getActivity() != null) {
                viewModel =  ViewModelProviders.of(getActivity()).get(Model.class);
            } else {
                Log.e("NavDialogs", "SaveDialog doesn't have a parent!");
                //Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
        return viewModel;
    }

    public static class Model extends NavDialogViewModel {
        /*private MutableLiveData<Pair<File, String>> fileData = new MutableLiveData<>();

        public MutableLiveData<Pair<File, String>> getFile() {
            return fileData;
        }

        void setData(File file, String encoding) {
            fileData.setValue(new Pair<>(file, encoding));
        }*/
        Callback callback;
        String fileName;
        String currentEncoding;

        private String[] availableEncodings = null;

        public String[] getAvailableEncodings() {
            if (availableEncodings == null) {
                availableEncodings = new String[Charset.availableCharsets().size()];
                Charset.availableCharsets().keySet().toArray(availableEncodings);
            }
            return availableEncodings;
        }
    }

    public interface Callback {
        void call(File file, String encoding);
    }

}