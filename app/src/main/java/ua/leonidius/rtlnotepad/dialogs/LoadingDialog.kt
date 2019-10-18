package ua.leonidius.rtlnotepad.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import ua.leonidius.rtlnotepad.R;

public class LoadingDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog.Builder pdb = new ProgressDialog.Builder(getActivity());
        pdb.setMessage(R.string.processing);
        pdb.setCancelable(false);
        return pdb.create();
    }

}