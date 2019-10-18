package ua.leonidius.rtlnotepad

import android.app.ActionBar

class EditorTabListener : ActionBar.TabListener {

    override fun onTabSelected(tab: ActionBar.Tab, ft: android.app.FragmentTransaction) {
        val fragment = tab.tag as EditorFragment
        val ft2 = MainActivity.instance.supportFragmentManager.beginTransaction()
        if (!fragment.isAdded) ft2.add(android.R.id.content, fragment, fragment.mTag)
        ft2.attach(fragment)
        ft2.commitAllowingStateLoss()
    }

    override fun onTabUnselected(tab: ActionBar.Tab, ft: android.app.FragmentTransaction) {
        val ft2 = MainActivity.instance.supportFragmentManager.beginTransaction()
        val fragment = tab.tag as EditorFragment
        ft2.detach(fragment)
        ft2.commitAllowingStateLoss()
    }

    override fun onTabReselected(p1: ActionBar.Tab, p2: android.app.FragmentTransaction) {}

}