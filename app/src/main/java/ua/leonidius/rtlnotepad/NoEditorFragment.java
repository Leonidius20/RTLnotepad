package ua.leonidius.rtlnotepad;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/**
* This fragment is being showed when there is no editor tabs opened.
* Author: Leonidius20
* Project: RTLnotepad
**/

public class NoEditorFragment extends Fragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.no_editor, container, false);
	}

}
