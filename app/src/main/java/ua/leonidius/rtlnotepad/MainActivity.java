package ua.leonidius.rtlnotepad;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import ua.leonidius.navdialogs.OpenDialog;
import ua.leonidius.rtlnotepad.dialogs.ExitDialog;
import ua.leonidius.rtlnotepad.dialogs.LastFilesDialog;
import ua.leonidius.rtlnotepad.utils.LastFilesMaster;

import java.io.File;
import java.util.LinkedHashMap;

public class MainActivity extends FragmentActivity {
    private NoEditorFragment noEditorFragment;
    public SharedPreferences pref;

    final int SIZE_SMALL = 14, SIZE_MEDIUM = 18, SIZE_LARGE = 22;
    final String PREF_TEXT_SIZE = "textSize";
    private final String PREF_THEME = "theme", PREF_THEME_LIGHT = "light", PREF_THEME_DARK = "dark";

    private final String BUNDLE_SELECTED_TAB_INDEX = "selectedTabIndex";

    public static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(null); // I'll manage to restore data on my own, thanks
        super.onCreate(savedInstanceState);
        // TODO: figure out a better way to restore data, cause passing null results im dialogFragments not being restored
        activity = this;
        pref = getPreferences(MODE_PRIVATE);

        // Applying dark theme if chosen
        if (pref.getString(PREF_THEME, PREF_THEME_LIGHT).equals(PREF_THEME_DARK)) {
            setTheme(R.style.Leonidius_Dark);
        }

        setContentView(new LinearLayout(this));

        // Setting up tab navigation
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        // Restoring tabs
        // TODO I temporarily commented this
		/*if (savedInstanceState != null) {
			restoreTabs(savedInstanceState);
		}*/

        // RESTORING TABS
        if (savedInstanceState != null) {
            LinkedHashMap<String, ActionBar.Tab> tabs = (LinkedHashMap<String, ActionBar.Tab>) getLastCustomNonConfigurationInstance();

            if (tabs.size() == 0) {
                noEditorFragment = (NoEditorFragment) getFragmentManager().findFragmentByTag(NoEditorFragment.TAG);
            } else {
                int selectedTabIndex = savedInstanceState.getInt(BUNDLE_SELECTED_TAB_INDEX, -1);
                for (String fragmentTag : tabs.keySet()) {
                    ActionBar.Tab tab = tabs.get(fragmentTag);
                    //tab.setTag(getFragmentManager().findFragmentByTag(fragmentTag));
                    // we don't change the fragment held in 'tag', because it is not destroyed
                    getActionBar().addTab(tab);
                    if (tab.getPosition() == selectedTabIndex) getActionBar().selectTab(tab);
                    // they reset fragment tags on recreate
                }
            }
        } else addNoEditorFragmentIfNeeded(); // If app is cold starting

        // Opening a file from intent
        // TODO check if activity was launched with the file or not
        String path;
        try {
            path = getIntent().getData().getSchemeSpecificPart();
            if (path != null) addTab(new File(path));
        } catch (Exception e) {
        }

        LastFilesMaster.initSlots(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
		/*removeNoEditorFragmentIfNeeded();
		HashSet<ActionBar.Tab> tabs = (HashSet<ActionBar.Tab>)getLastNonConfigurationInstance();
		for (ActionBar.Tab tab : tabs) {
			getActionBar().addTab(tab);
		}*/
    }

    /**
     * Opens a file in a new tab.
     *
     * @param file File to open
     */
    private void addTab(File file) {
        // Detaching noEditorFragment
        removeNoEditorFragmentIfNeeded();

        // If the file is already opened, switching to the file's tab
        // TODO: INSTEAD OF SWITCHING, PROMPT USER TO CHOOSE IF HE WANTS TO ADD A ANOTHER TAB WITH THAT FILE
        ActionBar.Tab fileTab = getFileTab(file);
        if (fileTab != null) {
            getActionBar().selectTab(fileTab);
            return;
        }

        // Creating a new tab
        ActionBar actionBar = getActionBar();
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(file.getName());

        EditorFragment fragment = new EditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EditorFragment.ARGUMENT_FILE_PATH, file.getPath());
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);
        tab.setTag(fragment); // Adding fragment as a tag

        tab.setTabListener(new EditorTabListener());
        actionBar.addTab(tab);

        // Selecting the tab
        actionBar.selectTab(tab);

        LastFilesMaster.add(file);
    }

    /**
     * Adds a tab with a blank editor for a new file
     */
    private void addTab() {
        // Detaching noEditorFragment
        removeNoEditorFragmentIfNeeded();

        ActionBar actionBar = getActionBar();
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText(R.string.new_document);

        EditorFragment fragment = new EditorFragment();
        fragment.setRetainInstance(true);
        tab.setTag(fragment); // Adding fragment as a tag

        tab.setTabListener(new EditorTabListener());
        actionBar.addTab(tab);

        // Selecting the tab
        actionBar.selectTab(tab);
    }

    /**
     * Closes a tab, but doesn't show any warnings if there are unsaved changes in the tab.
     * Is executed as a final stage of the tab closing process, after all warnings.
     *
     * @param tab Tab to close
     */
    public void closeTab(ActionBar.Tab tab) {
        getActionBar().removeTab(tab);
        addNoEditorFragmentIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_new, menu);
        switch (pref.getString(PREF_THEME, PREF_THEME_LIGHT)) {
            case PREF_THEME_LIGHT:
                menu.findItem(R.id.options_theme_light).setChecked(true);
                break;
            case PREF_THEME_DARK:
                menu.findItem(R.id.options_theme_dark).setChecked(true);
                break;
        }
        switch (pref.getInt(PREF_TEXT_SIZE, SIZE_MEDIUM)) {
            case SIZE_SMALL:
                menu.findItem(R.id.options_textSize_small).setChecked(true);
                break;
            case SIZE_MEDIUM:
                menu.findItem(R.id.options_textSize_medium).setChecked(true);
                break;
            case SIZE_LARGE:
                menu.findItem(R.id.options_textSize_large).setChecked(true);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_open:
                OpenDialog openDialog = new OpenDialog();
                openDialog.setCallback(this::addTab);
                openDialog.show(getSupportFragmentManager(), "openDialog");
                return true;
            case R.id.options_new:
                addTab();
                return true;
            case R.id.options_theme_light:
                setThemeNow(PREF_THEME_LIGHT, item);
                return true;
            case R.id.options_theme_dark:
                setThemeNow(PREF_THEME_DARK, item);
                return true;
            case R.id.options_last_files:
                LastFilesDialog lfd = new LastFilesDialog();
                lfd.setCallback(path -> addTab(new File(path)));
                lfd.show(getFragmentManager(), "lastFilesDialog");
                return true;
            case R.id.options_textSize_small:
                setTextSize(SIZE_SMALL);
                item.setChecked(true);
                return true;
            case R.id.options_textSize_medium:
                setTextSize(SIZE_MEDIUM);
                item.setChecked(true);
                return true;
            case R.id.options_textSize_large:
                setTextSize(SIZE_LARGE);
                item.setChecked(true);
                return true;
            case R.id.options_test:
                Intent i = new Intent();
                i.setClass(this, TestingActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        for (int i = 0; i < getActionBar().getTabCount(); i++) {
            ActionBar.Tab tab = getActionBar().getTabAt(i);
            EditorFragment tabFragment = (EditorFragment) tab.getTag();
            if (tabFragment.hasUnsavedChanges) {
                new ExitDialog().show(getFragmentManager(), "exitDialog");
                return;
            }
        }
        super.onBackPressed();
    }

    /**
     * Finds a tab in which the specified file is opened.
     *
     * @param file File which is opened in the tab we are looking for
     * @return ActionBar.Tab in which the specified file is opened, if such a tab exists, null otherwise
     */
    private ActionBar.Tab getFileTab(File file) {
        // Iterating over tabs
        for (int i = 0; i < getActionBar().getTabCount(); i++) {
            ActionBar.Tab tab = getActionBar().getTabAt(i);
            EditorFragment tabFragment = (EditorFragment) tab.getTag();
            if (tabFragment.file != null && tabFragment.file.equals(file)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Attaches a NoEditorFragment (placeholder) if all the tabs are closed.
     */
    // TODO: not to hold the fragment in memory?
    private void addNoEditorFragmentIfNeeded() {
        if (getActionBar().getTabCount() == 0) {
            FragmentTransaction sft = getFragmentManager().beginTransaction();
            if (noEditorFragment == null) noEditorFragment = new NoEditorFragment();
            if (!noEditorFragment.isAdded()) sft.add(android.R.id.content, noEditorFragment, NoEditorFragment.TAG);
            sft.attach(noEditorFragment);
            sft.commitAllowingStateLoss();
        }
    }

    /**
     * Removes a NoEditorFragment (placeholder) if a tab is being opened.
     */
    private void removeNoEditorFragmentIfNeeded() {
        if (getActionBar().getTabCount() == 0) {
            FragmentTransaction sft = getFragmentManager().beginTransaction();
            sft.detach(noEditorFragment);
            sft.commit();
        }
    }

    /**
     * Executed upon a click on a menu item that corresponds to one of the available themes. If the item was checked
     * with the click, saves a new theme setting and applies it instantly. Does nothing otherwise.
     *
     * @param theme A string that says which theme to apply. Must be one of the constants defined in MainActivity
     * @param item  An item of the menu which corresponds to a theme.
     */
    private void setThemeNow(String theme, MenuItem item) {
        if (!item.isChecked()) {
            item.setChecked(true);
            SharedPreferences.Editor prefEdit = pref.edit();
            prefEdit.putString(PREF_THEME, theme);
            prefEdit.apply();
            recreate();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getActionBar().getTabCount() > 0) {
            outState.putInt(BUNDLE_SELECTED_TAB_INDEX, getActionBar().getSelectedTab().getPosition());
        }

        LastFilesMaster.saveSlots(this);
    }

    /**
     * @return An instance of MainActivity
     */
    public static MainActivity getInstance() {
        return activity;
    }

    /**
     * Sets a specified text size for all opened editors and saves the new settings to preferences.
     *
     * @param size The text size to apply
     */
    private void setTextSize(int size) {
        for (int i = 0; i < getActionBar().getTabCount(); i++) {
            ActionBar.Tab tab = getActionBar().getTabAt(i);
            EditorFragment fragment = (EditorFragment) tab.getTag();
            fragment.getEditor().setTextSize(size);
        }
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putInt(PREF_TEXT_SIZE, size);
        prefEditor.apply();
        // TODO: Consider recreating the activity at that point to avoid iterating through tabs
    }

    @Nullable
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        int tabCount = getActionBar().getTabCount();
        LinkedHashMap<String, ActionBar.Tab> tabs = new LinkedHashMap<>();
        for (int i = 0; i < tabCount; i++) {
            ActionBar.Tab tab = getActionBar().getTabAt(i);
            tabs.put(((EditorFragment) tab.getTag()).getTag(), tab);
        }
        return tabs;
    }

}