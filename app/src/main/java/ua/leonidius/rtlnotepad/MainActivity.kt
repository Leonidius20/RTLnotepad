package ua.leonidius.rtlnotepad

import android.app.ActionBar
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import ua.leonidius.navdialogs.LegacyOpenDialog
import ua.leonidius.rtlnotepad.dialogs.ExitDialog
import ua.leonidius.rtlnotepad.dialogs.LastFilesDialog
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog
import ua.leonidius.rtlnotepad.dialogs.WrongFileTypeDialog
import ua.leonidius.rtlnotepad.utils.addToLastFiles
import ua.leonidius.rtlnotepad.utils.getFileName
import ua.leonidius.rtlnotepad.utils.takePersistablePermissions
import java.util.*

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        // Applying dark theme if chosen
        if (Settings.theme == Settings.PREF_THEME_DARK) {
            setTheme(R.style.Leonidius_Dark)
        }

        setContentView(LinearLayout(this))

        // Setting up tab navigation
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        actionBar!!.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) { // Cold start
            intent.data?.let { addTab(it) }
            addPlaceholderFragmentIfNeeded()
            return
        }

        // Restoring tabs after activity recreation
        val tabs = lastCustomNonConfigurationInstance as LinkedHashMap<String, ActionBar.Tab>
        if (tabs.size != 0) {
            val selectedTabIndex = savedInstanceState.getInt(BUNDLE_SELECTED_TAB_INDEX, -1)
            for (tab in tabs.values) {
                with (actionBar!!) {
                    addTab(tab)
                    if (tab.position == selectedTabIndex) selectTab(tab)
                }
            }
        }
    }

    /**
     * Opens the file with the given Uri in a new tab
     */
    private fun addTab(uri: Uri) {
        // Checking if the file is already opened
        getFileTab(uri)?.also {
            actionBar!!.selectTab(it)
            return
        }

        removePlaceholderFragmentIfNeeded()

        // Getting the name of the file
        val displayName = getFileName(applicationContext, uri)

        // Creating a new fragment
        val fragment = EditorFragment().apply {
            arguments = Bundle().also { it.putParcelable(EditorFragment.ARGUMENT_URI, uri) }
            retainInstance = true
        }

        // Creating a new tab
        val tab = actionBar!!.newTab().apply {
            text = displayName
            tag = fragment
            setTabListener(EditorTabListener())
        }

        with (actionBar!!) {
            addTab(tab)
            selectTab(tab)
        }

        addToLastFiles(uri)
    }

    /**
     * Adds a tab with a blank editor for a new file
     */
    private fun addTab() {
        removePlaceholderFragmentIfNeeded()

        val fragment = EditorFragment().apply { retainInstance = true }

        val tab = actionBar!!.newTab().apply {
            setText(R.string.new_document)
            tag = fragment
            setTabListener(EditorTabListener())
        }

        with (actionBar!!) {
            addTab(tab)
            selectTab(tab)
        }
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
        menuInflater.inflate(R.menu.options_main, menu)
        when (Settings.theme) {
            Settings.PREF_THEME_LIGHT -> menu.findItem(R.id.options_theme_light).isChecked = true
            Settings.PREF_THEME_DARK -> menu.findItem(R.id.options_theme_dark).isChecked = true
        }
        when (Settings.textSize) {
            Settings.SIZE_SMALL -> menu.findItem(R.id.options_textSize_small).isChecked = true
            Settings.SIZE_MEDIUM -> menu.findItem(R.id.options_textSize_medium).isChecked = true
            Settings.SIZE_LARGE -> menu.findItem(R.id.options_textSize_large).isChecked = true
        }
        if (Settings.useLegacyDialogs) {
            menu.findItem(R.id.options_useLegacyDialogs).isChecked = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.options_open -> {
                openFile()
                return true
            }
            R.id.options_new -> {
                addTab()
                return true
            }
            R.id.options_theme_light -> {
                setThemeNow(Settings.PREF_THEME_LIGHT, item)
                return true
            }
            R.id.options_theme_dark -> {
                setThemeNow(Settings.PREF_THEME_DARK, item)
                return true
            }
            R.id.options_last_files -> {
                LastFilesDialog.create {
                    instance.addTab(it)
                }.show(supportFragmentManager, "lastFilesDialog")
                return true
            }
            R.id.options_textSize_small -> {
                setTextSize(Settings.SIZE_SMALL)
                item.isChecked = true
                return true
            }
            R.id.options_textSize_medium -> {
                setTextSize(Settings.SIZE_MEDIUM)
                item.isChecked = true
                return true
            }
            R.id.options_textSize_large -> {
                setTextSize(Settings.SIZE_LARGE)
                item.isChecked = true
                return true
            }
            R.id.options_loadingDialog -> {
                LoadingDialog().show(supportFragmentManager, "loadingDialog")
                return true
            }
            R.id.options_useLegacyDialogs -> {
                item.isChecked = !item.isChecked
                Settings.useLegacyDialogs = item.isChecked
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openFile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || Settings.useLegacyDialogs) {
            lateinit var dialog: LegacyOpenDialog
            dialog = LegacyOpenDialog.create { uri ->
                if (!isText(uri)) {
                    WrongFileTypeDialog.create {
                        if (it) instance.addTab(uri)
                        else dialog.show(instance.supportFragmentManager, "openDialog")
                    }.show(instance.supportFragmentManager, "WFTDialog")
                } else {
                    // we use 'instance' because otherwise it adds a new
                    // fragment to the old activity after orientation change
                    instance.addTab(uri)
                }
            }
            dialog.show(supportFragmentManager, "openDialog")
            return
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, PICK_TEXT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            PICK_TEXT_FILE -> {
                resultData?.data?.also { uri ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        takePersistablePermissions(this, uri)
                    }
                    addTab(uri)
                }
            }
        }
    }

    override fun onBackPressed() {
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            val tabFragment = tab.tag as EditorFragment
            if (tabFragment.hasUnsavedChanges) {
                ExitDialog.create { exit ->
                    if (exit) finish()
                }.show(supportFragmentManager, "exitDialog")
                return
            }
        }
        super.onBackPressed()
    }

    /**
     * Finds a tab in which the specified file is opened.
     *
     * @param uriToFind File which is opened in the tab we are looking for
     * @return ActionBar.Tab in which the specified file is opened, if such a tab exists, null otherwise
     */
    private fun getFileTab(uriToFind: Uri) : ActionBar.Tab? {
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            if ((tab.tag as EditorFragment).run { uri != null && uri!! == uriToFind }) return tab
        }
        return null
    }

    /**
     * Attaches a PlaceholderFragment if all the tabs are closed.
     */
    private fun addPlaceholderFragmentIfNeeded() {
        if (actionBar!!.tabCount == 0) {
            val placeholder = supportFragmentManager.findFragmentByTag(PlaceholderFragment.TAG) ?: PlaceholderFragment()
            with (supportFragmentManager.beginTransaction()) {
                if (!placeholder.isAdded) add(android.R.id.content, placeholder, PlaceholderFragment.TAG)
                attach(placeholder)
                commitAllowingStateLoss()
            }
        }
    }

    /**
     * Removes a PlaceholderFragment if a tab is being opened.
     */
    private fun removePlaceholderFragmentIfNeeded() {
        if (actionBar!!.tabCount == 0) {
            supportFragmentManager.findFragmentByTag(PlaceholderFragment.TAG).let {
                if (it != null && !it.isDetached) {
                    with (supportFragmentManager.beginTransaction()) {
                        detach(it)
                        commitAllowingStateLoss()
                    }
                }
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
            Settings.theme = theme
            recreate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (actionBar!!.tabCount > 0) {
            outState.putInt(BUNDLE_SELECTED_TAB_INDEX, actionBar!!.selectedTab.position)
        }
    }

    /**
     * Sets a specified text size for all opened editors and saves the new settings to preferences.
     *
     * @param size The text size to apply
     */
    private fun setTextSize(size: Int) {
        for (i in 0 until actionBar!!.tabCount) {
            (actionBar!!.getTabAt(i).tag as EditorFragment).setEditorTextSize(size.toFloat())
        }
        Settings.textSize = size
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        val tabs = LinkedHashMap<String, ActionBar.Tab>()
        for (i in 0 until actionBar!!.tabCount) {
            val tab = actionBar!!.getTabAt(i)
            tabs[(tab.tag as EditorFragment).tag!!] = tab
        }
        return tabs
    }

    companion object {

        // Request codes
        const val PICK_TEXT_FILE = 0

        private const val BUNDLE_SELECTED_TAB_INDEX = "selectedTabIndex"

       lateinit var instance: MainActivity

        private fun isText(uri: Uri): Boolean {
            return try {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))!!.split("/")[0] == "text";
            } catch (e: Exception) {
                false
            }
        }

    }

}