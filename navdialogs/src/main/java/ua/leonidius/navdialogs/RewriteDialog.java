package ua.leonidius.navdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

public class RewriteDialog extends DialogFragment implements AlertDialog.OnClickListener {

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

    private void callback(boolean value) {
        Model model;
        if (getParentFragment() != null) { // TODO: maybe create a helper class that has a function for finding fragment's specific viewmodels
            model = ViewModelProviders.of(getParentFragment()).get(Model.class);
        } else if (getActivity() != null) {
            model = ViewModelProviders.of(getActivity()).get(Model.class);
        } else {
            Log.d("NavDialogs", "RewriteDialog doesn't have a parent!");
            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
            return;
        }

        model.setRewrite(value);
    }

    static class Model extends ViewModel {
        private MutableLiveData<Boolean> rewrite = new MutableLiveData<>();

        void setRewrite(boolean value) {
            rewrite.setValue(value);
        }

        LiveData<Boolean> rewrite() {
            return rewrite;
        }

    }

}