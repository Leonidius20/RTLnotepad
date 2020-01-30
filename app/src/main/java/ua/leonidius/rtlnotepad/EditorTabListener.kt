package ua.leonidius.rtlnotepad

import com.google.android.material.tabs.TabLayout

class EditorTabListener() : TabLayout.OnTabSelectedListener {

    // We can't stop using MainActivity.instance here, because otherwise
    // after orientation change the previous activity is destroyed and
    // when we try to detach a fragment from it, the program crashes
    // because the fragment was attached to a different activity.

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val ft2 = MainActivity.instance.supportFragmentManager.beginTransaction()
        val fragment = MainActivity.instance.supportFragmentManager.findFragmentByTag(tab!!.tag as String)!!
        if (!fragment.isAdded) ft2.add(android.R.id.content, fragment, tab.tag as String)
        ft2.attach(fragment)
        ft2.commitAllowingStateLoss()
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val ft2 = MainActivity.instance.supportFragmentManager.beginTransaction()
        val fragment = MainActivity.instance.supportFragmentManager.findFragmentByTag(tab!!.tag as String)!!
        ft2.detach(fragment)
        ft2.commitAllowingStateLoss()
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {}

}