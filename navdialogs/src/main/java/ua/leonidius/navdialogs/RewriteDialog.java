package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener {

    //private Callback callback;

    @Override
    public void onClick(DialogInterface p1, int id) {
        switch (id) {
            case Dialog.BUTTON_NEGATIVE:
                //callback.callback(false);
                callback(false);
                break;
            case Dialog.BUTTON_POSITIVE:
                //callback.callback(true);
                callback(true);
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setCancelable(false);
        adb.setMessage(R.string.file_overwrite);
        adb.setNegativeButton(android.R.string.no, this);
        adb.setPositiveButton(android.R.string.yes, this);
        return adb.create();
    }

    private void callback(boolean value) {
        if (getParentFragment() == null) {
            Log.d("NavDialogs", "RewriteDialog doesn't have a parent!");
            return;
        }
        RewriteSaveViewModel model;
        if (getParentFragment().getActivity() != null) { // TODO: maybe create a helper class that has a function for finding fragment's specific viewmodels
            model = ViewModelProviders.of(getParentFragment().getActivity()).get(RewriteSaveViewModel.class);
        } else if (getParentFragment().getParentFragment() != null) {
            model = ViewModelProviders.of(getParentFragment().getParentFragment()).get(RewriteSaveViewModel.class);
        } else {
            Log.d("NavDialogs", "SaveDialog doesn't have a parent!");
            return;
        }

        model.setRewrite(value);
    }

    public static class RewriteSaveViewModel extends ViewModel {
        private MutableLiveData<Boolean> rewrite = new MutableLiveData<>();

        public void setRewrite(boolean value) {
            rewrite.setValue(value);
        }

        public LiveData<Boolean> rewrite() {
            return rewrite;
        }

    }

}