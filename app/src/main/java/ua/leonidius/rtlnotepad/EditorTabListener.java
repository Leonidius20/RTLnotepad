package ua.leonidius.rtlnotepad;
import android.app.*;
import android.app.ActionBar.*;
import android.util.*;
import android.widget.*;
import android.os.*;

public class EditorTabListener implements ActionBar.TabListener
{
	
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
	{
		EditorFragment fragment = (EditorFragment)tab.getTag();
		FragmentTransaction ft2 = TestActivity.getInstance().getFragmentManager().beginTransaction();
		ft2.add(android.R.id.content, fragment, fragment.mTag);
		ft2.attach(fragment);
		ft2.commitAllowingStateLoss();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
	{
		FragmentTransaction ft2 = TestActivity.getInstance().getFragmentManager().beginTransaction();
		Fragment fragment = (EditorFragment)tab.getTag();
		ft2.detach(fragment);
		ft2.commitAllowingStateLoss();
	}

	@Override
	public void onTabReselected(ActionBar.Tab p1, FragmentTransaction p2) {}

}
