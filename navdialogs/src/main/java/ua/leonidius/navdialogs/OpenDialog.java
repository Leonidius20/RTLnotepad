package ua.leonidius.navdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

public class OpenDialog extends NavigationDialog {

    private Model viewModel = null;
    private Initializer initializer = new Initializer();

    public static OpenDialog create(Callback callback) {
        return create(Environment.getExternalStorageDirectory(), callback);
    }

    public static OpenDialog create(File defaultDir, Callback callback) {
        OpenDialog dialog = new OpenDialog();;
        dialog.initializer.defaultDir = defaultDir;
        dialog.initializer.callback = callback;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: instead of a header put a horizontal layout with "up" and "create folder" options
        return inflater.inflate(R.layout.navdialogs_dialog_open, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view, savedInstanceState);
        openDir(getViewModel().currentDir);
    }

    @Override
    protected void onFileClick(File file) {
        getViewModel().callback.call(file);
        getDialog().dismiss();
    }

    @Override
    public Model getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(Model.class);
        }
        return viewModel;
    }

    /*private boolean isText(File file) {
        try {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())).split("/")[0].equals("text");
        } catch (Exception e) {
            return false;
        }
    }*/

    public interface Callback {
        void call(File file);
    }

    public static class Model extends NavDialogViewModel {
        boolean initialized = false;
        Callback callback;
    }

    private class Initializer {
        File defaultDir;
        Callback callback;

        void initialize() {
            getViewModel().currentDir = defaultDir;
            getViewModel().callback = callback;
        }
    }

}