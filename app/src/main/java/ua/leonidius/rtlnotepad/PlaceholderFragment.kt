package ua.leonidius.rtlnotepad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * This fragment is being showed when there is no editor tabs opened.
 *
 * @author Leonidius20
 * Project: RTLnotepad
 */

class PlaceholderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.no_editor, container, false)
    }

    companion object {

        internal val TAG = "noEditorFragment"
    }

}
