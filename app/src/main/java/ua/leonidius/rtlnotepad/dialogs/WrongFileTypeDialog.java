package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ua.leonidius.rtlnotepad.R;

public class WrongFileTypeDialog extends DialogFragment implements AlertDialog.OnClickListener {

    private Callback callback;

    @Override
    public void onClick(DialogInterface p1, int id) {
        switch (id) {
            case Dialog.BUTTON_NEGATIVE:
                callback.callback(false);
                break;
            case Dialog.BUTTON_POSITIVE:
                callback.callback(true);
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setCancelable(false);
        adb.setMessage(R.string.wrong_file_type_warning);
        adb.setNegativeButton(android.R.string.no, this);
        adb.setPositiveButton(android.R.string.yes, this);
        return adb.create();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void callback(boolean open);
    }

}