package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener {

    private Model viewModel = null;

    public static RewriteDialog create(Fragment frag, Callback callback) {
        return create(ViewModelProviders.of(frag).get(Model.class), callback);
    }

    public static RewriteDialog create(FragmentActivity frag, Callback callback) {
        return create(ViewModelProviders.of(frag).get(Model.class), callback);
    }

    private static RewriteDialog create(Model model, Callback callback) {
        RewriteDialog dialog = new RewriteDialog();
        dialog.viewModel = model;
        dialog.viewModel.callback = callback;
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
            if (getParentFragment() != null) { // TODO: maybe create a helper class that has a function for finding fragment's specific viewmodels
                viewModel = ViewModelProviders.of(getParentFragment()).get(Model.class);
            } else if (getActivity() != null) {
                viewModel = new ViewModelProvider(getActivity()).get(Model.class);
            } else {
                Log.d("NavDialogs", "RewriteDialog doesn't have a parent!");
            }
        }
        return viewModel;
    }

    public interface Callback {
        void call(boolean rewrite);
    }

    public static class Model extends ViewModel {
        Callback callback;
    }

}