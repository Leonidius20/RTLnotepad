package ua.leonidius.navdialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;

public class OpenDialog extends NavigationDialog {

    private Model viewModel = null;

    public static OpenDialog create(Fragment frag, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, Environment.getExternalStorageDirectory(), callback);
    }

    public static OpenDialog create(FragmentActivity frag, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, Environment.getExternalStorageDirectory(), callback);
    }

    public static OpenDialog create(Fragment frag, File defaultDir, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultDir, callback);
    }

    public static OpenDialog create(FragmentActivity frag, File defaultDir, Callback callback) {
        Model model = ViewModelProviders.of(frag).get(Model.class);
        return create(model, defaultDir, callback);
    }

    private static OpenDialog create(Model model, File defaultDir, Callback callback) {
        OpenDialog dialog = new OpenDialog();
        dialog.viewModel = model;
        dialog.viewModel.currentDir = defaultDir;
        dialog.viewModel.callback = callback;
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
        /*if (!isText(file)) {
            WrongFileTypeDialog wftd = new WrongFileTypeDialog();
            wftd.setCallback(open -> {
                if (open) {
                    callback.callback(file);
                    getDialog().cancel();
                }
            });
            wftd.show(getActivity().getFragmentManager(), "wrongFileTypeDialog");
            return;
        }*/

        getViewModel().callback.call(file);
        getDialog().dismiss();
    }

    @Override
    public Model getViewModel() {
        if (viewModel == null) {
            Log.d("NavDialogs (openD)", "getActivity() is null: " + (getActivity() == null)
                    + ", getParentFragment() is null: " + (getParentFragment() == null));
            if (getParentFragment() != null) {
                viewModel = ViewModelProviders.of(getParentFragment()).get(Model.class);
            } else if (getActivity() != null) {
                viewModel = ViewModelProviders.of(getActivity()).get(Model.class);
            } else {
                Log.e("NavDialogs", "SaveDialog doesn't have a parent!");
                //Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
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
        Callback callback;
    }

}