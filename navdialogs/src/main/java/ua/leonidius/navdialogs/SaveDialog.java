package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveDialog extends NavigationDialog implements AlertDialog.OnClickListener {

    private EditText nameField;
    private Spinner encodingSpinner;
    private Model viewModel = null;
    private Initializer initializer = new Initializer();

    public static SaveDialog create(String defaultName, String defaultEncoding,  Callback callback) {
        return create(defaultName, defaultEncoding, Environment.getExternalStorageDirectory(), callback);
    }

    public static SaveDialog create(String defaultName, String defaultEncoding, File defaultDirectory, Callback callback) {
        SaveDialog dialog = new SaveDialog();
        dialog.initializer.defaultEncoding = defaultEncoding;
        dialog.initializer.defaultFileName = defaultName;
        dialog.initializer.defaultDir = defaultDirectory;
        dialog.initializer.callback = callback;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!getViewModel().initialized) {
            initializer.initialize();
            getViewModel().initialized = true;
        }
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

        RewriteDialog.create(rewrite -> {
            if (rewrite) getViewModel().callback.call(file, encoding);
            else show(getFragmentManager(), "saveDialog");
        }).show(getFragmentManager(), "rewriteDialog");
    }

    @Override
    public Model getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(Model.class);
        }
        return viewModel;
    }

    public static class Model extends NavDialogViewModel {
        boolean initialized = false;
        Callback callback;
        String fileName;
        String currentEncoding;

        private String[] availableEncodings = null;

        String[] getAvailableEncodings() {
            if (availableEncodings == null) {
                availableEncodings = new String[Charset.availableCharsets().size()];
                Charset.availableCharsets().keySet().toArray(availableEncodings);
            }
            return availableEncodings;
        }
    }

    private class Initializer {
        File defaultDir;
        String defaultFileName;
        String defaultEncoding;
        Callback callback;

        void initialize() {
            getViewModel().currentDir = defaultDir;
            getViewModel().fileName = defaultFileName;
            getViewModel().currentEncoding = defaultEncoding;
            getViewModel().callback = callback;
        }
    }

    public interface Callback {
        void call(File file, String encoding);
    }

}