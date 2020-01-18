package ua.leonidius.rtlnotepad

import android.app.ActionBar
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import ua.leonidius.navdialogs.OpenDialog
import ua.leonidius.rtlnotepad.dialogs.ExitDialog
import ua.leonidius.rtlnotepad.dialogs.LastFilesDialog
import ua.leonidius.rtlnotepad.utils.LastFilesMaster
import java.io.File
import java.util.*

class MainActivity : FragmentActivity() {
    //private PlaceholderFragment placeholderFragment;
    lateinit var pref: SharedPreferences

    internal val SIZE_SMALL = 14
    internal val SIZE_MEDIUM = 18
    internal val SIZE_LARGE = 22
    internal val PREF_TEXT_SIZE = "textSize"
    private val PREF_THEME = "theme"
    private val PREF_THEME_LIGHT = "light"
    private val PREF_THEME_DARK = "dark"

    private val BUNDLE_SELECTED_TAB_INDEX = "selectedTabIndex"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        pref = getPreferences(Context.MODE_PRIVATE)

        // Applying dark theme if chosen
        if (pref.getString(PREF_THEME, PREF_THEME_LIGHT) == PREF_THEME_DARK) {
            setTheme(R.style.Leonidius_Dark)
        }

        setContentView(LinearLayout(this))

        // Setting up tab navigation
        val actionBar = actionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        actionBar.setDisplayShowTitleEnabled(false)

        LastFilesMaster.initSlots(this)

        if (savedInstanceState == null) { // Cold start
            addPlaceholderFragmentIfNeeded()
            // Opening a file from intent
            if (intent.data != null) {
                val path = intent.data!!.schemeSpecificPart
                if (path != null) addTab(File(path))
            }
        } else { // Restoring tabs after activity recreation
            val tabs = lastCustomNonConfigurationInstance as LinkedHashMap<String, ActionBar.Tab>?
            if (tabs!!.size != 0) {
                val selectedTabIndex = savedInstanceState.getInt(BUNDLE_SELECTED_TAB_INDEX, -1)
                for (fragmentTag in tabs.keys) {
                    val tab = tabs[fragmentTag]
                    //tab.setTag(getFragmentManager().findFragmentByTag(fragmentTag));
                    // we don't change the fragment held in 'tag', because it is not destroyed
                    getActionBar()!!.addTab(tab)
                    if (tab!!.position == selectedTabIndex) getActionBar()!!.selectTab(tab)
                    // they reset fragment tags on recreate?
                }
            }
        }
    }

    /**
     * Opens a file in a new tab.
     *
     * @param file File to open
     */
    private fun addTab(file: File) {
        removePlaceholderFragmentIfNeeded()

        // If the file is already opened, switching to the file's tab
        // TODO: INSTEAD OF SWITCHING, PROMPT USER TO CHOOSE IF HE WANTS TO ADD A ANOTHER TAB WITH THAT FILE
        val fileTab = getFileTab(file)
        if (fileTab != null) {
            actionBar!!.selectTab(fileTab)
            return
        }

        // Creating a new tab
        val actionBar = actionBar
        val tab = actionBar!!.newTab()
        tab.text = file.name

        val fragment = EditorFragment()
        val arguments = Bundle()
        arguments.putString(EditorFragment.ARGUMENT_FILE_PATH, file.path)
        fragment.arguments = arguments
        fragment.retainInstance = true
        tab.tag = fragment

        tab.setTabListener(EditorTabListener())
        actionBar.addTab(tab)

        // Selecting the tab
        actionBar.selectTab(tab)

        LastFilesMaster.add(file)
    }

    /**
     * Adds a tab with a blank editor for a new file
     */
    private fun addTab() {
        // Detaching noEditorFragment
        removePlaceholderFragmentIfNeeded()

        val actionBar = actionBar
        val tab = actionBar!!.newTab()
        tab.setText(R.string.new_document)

        val fragment = EditorFragment()
        fragment.retainInstance = true
        tab.tag = fragment

        tab.setTabListener(EditorTabListener())
        actionBar.addTab(tab)

        // Selecting the tab
        actionBar.selectTab(tab)
    }

    /**
     * Closes a tab, but doesn't show any warnings if there are unsaved changes in the tab.
     * Is executed as a final stage of the tab closing process, after all warnings.
     *
     * @param tab Tab to close
     */
    fun closeTab(tab: ActionBar.Tab) {
        actionBar!!.removeTab(tab)
        addPlaceholderFragmentIfNeeded()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_new, menu)
        when (pref.getString(PREF_THEME, PREF_THEME_LIGHT)) {
            PREF_THEME_LIGHT -> menu.findItem(R.id.options_theme_light).isChecked = true
            PREF_THEME_DARK -> menu.findItem(R.id.options_theme_dark).isChecked = true
        }
        when (pref.getInt(PREF_TEXT_SIZE, SIZE_MEDIUM)) {
            SIZE_SMALL -> menu.findItem(R.id.options_textSize_small).isChecked = true
            SIZE_MEDIUM -> menu.findItem(R.id.options_textSize_medium).isChecked = true
            SIZE_LARGE -> menu.findItem(R.id.options_textSize_large).isChecked = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.options_open -> {
                OpenDialog.create { file : File -> addTab(file) }.show(supportFragmentManager, "openDialog")
                return true
            }
            R.id.options_new -> {
                addTab()
                return true
            }
            R.id.options_theme_light -> {
                setThemeNow(PREF_THEME_LIGHT, item)
                return true
            }
            R.id.options_theme_dark -> {
                setThemeNow(PREF_THEME_DARK, item)
                return true
            }
            R.id.options_last_files -> {
                // TODO("Replace with concise syntax")
                val lfd = LastFilesDialog()
                lfd.setCallback { path -> addTab(File(path)) }
                lfd.show(supportFragmentManager, "lastFilesDialog")
                return true
            }
            R.id.options_textSize_small -> {
                setTextSize(SIZE_SMALL)
                item.isChecked = true
                return true
            }
            R.id.options_textSize_medium -> {
                setTextSize(SIZE_MEDIUM)
                item.isChecked = true
                return true
            }
            R.id.options_textSize_large -> {
                setTextSize(SIZE_LARGE)
                item.isChecked = true
                return true
            }
        }/*case R.id.options_test:
                Intent i = new Intent();
                i.setClass(this, TestingActivity.class);
                startActivity(i);
                return true;*/
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            val tabFragment = tab.tag as EditorFragment
            if (tabFragment.hasUnsavedChanges) {
                ExitDialog().show(supportFragmentManager, "exitDialog")
                return
            }
        }
        super.onBackPressed()
    }

    /**
     * Finds a tab in which the specified file is opened.
     *
     * @param file File which is opened in the tab we are looking for
     * @return ActionBar.Tab in which the specified file is opened, if such a tab exists, null otherwise
     */
    private fun getFileTab(file: File): ActionBar.Tab? {
        // Iterating over tabs
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            val tabFragment = tab.tag as EditorFragment
            if (tabFragment.file != null && tabFragment.file == file) {
                return tab
            }
        }
        return null
    }

    /**
     * Attaches a PlaceholderFragment if all the tabs are closed.
     */
    private fun addPlaceholderFragmentIfNeeded() {
        if (actionBar!!.tabCount == 0) {
            var placeholder = supportFragmentManager.findFragmentByTag(PlaceholderFragment.TAG)
            if (placeholder == null) {
                placeholder = PlaceholderFragment()
            }

            val sft = supportFragmentManager.beginTransaction()
            if (!placeholder.isAdded) sft.add(android.R.id.content, placeholder, PlaceholderFragment.TAG)
            sft.attach(placeholder)
            sft.commitAllowingStateLoss()
        }
    }

    /**
     * Removes a PlaceholderFragment if a tab is being opened.
     */
    private fun removePlaceholderFragmentIfNeeded() {
        if (actionBar!!.tabCount == 0) {
            val placeholder = supportFragmentManager.findFragmentByTag(PlaceholderFragment.TAG)
            if (placeholder != null && !placeholder.isDetached) {
                val sft = supportFragmentManager.beginTransaction()
                sft.detach(placeholder)
                sft.commitAllowingStateLoss()
            }
        }
    }

    /**
     * Executed upon a click on a menu item that corresponds to one of the available themes. If the item was checked
     * with the click, saves a new theme setting and applies it instantly. Does nothing otherwise.
     *
     * @param theme A string that says which theme to apply. Must be one of the constants defined in MainActivity
     * @param item  An item of the menu which corresponds to a theme.
     */
    private fun setThemeNow(theme: String, item: MenuItem) {
        if (!item.isChecked) {
            item.isChecked = true
            val prefEdit = pref.edit()
            prefEdit.putString(PREF_THEME, theme)
            prefEdit.apply()
            recreate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (actionBar!!.tabCount > 0) {
            outState.putInt(BUNDLE_SELECTED_TAB_INDEX, actionBar!!.selectedTab.position)
        }

        LastFilesMaster.saveSlots(this)
    }

    /**
     * Sets a specified text size for all opened editors and saves the new settings to preferences.
     *
     * @param size The text size to apply
     */
    private fun setTextSize(size: Int) {
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            val fragment = tab.tag as EditorFragment
            fragment.setEditorTextSize(size.toFloat())
        }
        val prefEditor = pref.edit()
        prefEditor.putInt(PREF_TEXT_SIZE, size)
        prefEditor.apply()
        // TODO: Consider recreating the activity at that point to avoid iterating through tabs
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        val tabCount = actionBar!!.tabCount
        val tabs = LinkedHashMap<String, ActionBar.Tab>()
        for (i in 0 until tabCount) {
            val tab = actionBar!!.getTabAt(i)
            tabs[(tab.tag as EditorFragment).tag!!] = tab
        }
        return tabs
    }

    companion object {

        /**
         * @return An instance of MainActivity
         */
        lateinit var instance: MainActivity
    }

}