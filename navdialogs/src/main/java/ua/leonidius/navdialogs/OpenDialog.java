package ua.leonidius.navdialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;

import java.io.File;

public class OpenDialog extends NavigationDialog {

    private Callback callback;

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
        openDir(currentDir);
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

        callback.callback(file);
        getDialog().dismiss();
    }

    /*private boolean isText(File file) {
        try {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())).split("/")[0].equals("text");
        } catch (Exception e) {
            return false;
        }
    }*/

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void callback(File file);
    }

}