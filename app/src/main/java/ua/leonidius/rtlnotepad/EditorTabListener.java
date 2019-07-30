package ua.leonidius.rtlnotepad;

import android.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;

public class EditorTabListener implements ActionBar.TabListener {

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        EditorFragment fragment = (EditorFragment) tab.getTag();
        FragmentTransaction ft2 = MainActivity.getInstance().getSupportFragmentManager().beginTransaction();
        if (!fragment.isAdded()) ft2.add(android.R.id.content, fragment, fragment.mTag);
        ft2.attach(fragment);
        ft2.commitAllowingStateLoss();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        FragmentTransaction ft2 = MainActivity.getInstance().getSupportFragmentManager().beginTransaction();
        EditorFragment fragment = (EditorFragment) tab.getTag();
        ft2.detach(fragment);
        ft2.commitAllowingStateLoss();
    }

    @Override
    public void onTabReselected(ActionBar.Tab p1, android.app.FragmentTransaction p2) {
    }

}