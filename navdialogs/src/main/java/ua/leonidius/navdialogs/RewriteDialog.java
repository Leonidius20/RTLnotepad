package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener {

    private Model viewModel = null;
    private Initializer initializer = new Initializer();

    public static RewriteDialog create(Callback callback) {
        RewriteDialog dialog = new RewriteDialog();
        dialog.initializer.callback = callback;
        return dialog;
    }

    @Override
    public void onClick(DialogInterface p1, int id) {
        switch (id) {
            case Dialog.BUTTON_NEGATIVE:
                getViewModel().callback.call(false);
                break;
            case Dialog.BUTTON_POSITIVE:
                getViewModel().callback.call(true);
                break;
        }
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
        adb.setCancelable(false);
        adb.setMessage(R.string.file_overwrite);
        adb.setNegativeButton(android.R.string.no, this);
        adb.setPositiveButton(android.R.string.yes, this);
        return adb.create();
    }

    private Model getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(Model.class);
        }
        return viewModel;
    }

    public interface Callback {
        void call(boolean rewrite);
    }

    public static class Model extends ViewModel {
        boolean initialized = false;
        Callback callback;
    }

    public class Initializer {
        Callback callback;

        void initialize() {
            getViewModel().callback = callback;
        }
    }

}