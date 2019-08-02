package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import ua.leonidius.rtlnotepad.EditorFragment;
import ua.leonidius.rtlnotepad.R;

public class PermissionRequestDialog extends DialogFragment implements AlertDialog.OnClickListener {

    private int type;
    public static String TYPE = "type";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        type = getArguments().getInt(TYPE);
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());

        if (type == EditorFragment.READ_PERMISSION_CODE) {
            adb.setMessage(R.string.grant_read_permissions);
        } else {
            adb.setMessage(R.string.grant_write_permissions);
        }

        adb.setPositiveButton(android.R.string.yes, this);
        adb.setNegativeButton(android.R.string.no, this);

        return adb.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            if (type == EditorFragment.READ_PERMISSION_CODE) {
                ((EditorFragment) getParentFragment()).tryReadingFileAgain();
            } else ((EditorFragment) getParentFragment()).tryWritingFileAgain();
        }
    }

}