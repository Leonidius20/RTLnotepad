package ua.leonidius.rtlnotepad;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This fragment is being showed when there is no editor tabs opened.
 *
 * @author Leonidius20
 * Project: RTLnotepad
 **/

public class NoEditorFragment extends Fragment {

    final static String TAG = "noEditorFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.no_editor, container, false);
    }

}
