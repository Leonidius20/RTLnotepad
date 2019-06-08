package ua.leonidius.rtlnotepad.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

public class LoadingDialog extends DialogFragment  {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.getInstance());
        adb.setCancelable(false);
        adb.setView(MainActivity.getInstance().getLayoutInflater().inflate(R.layout.dialog_loading, null));
        return adb.create();
    }
}