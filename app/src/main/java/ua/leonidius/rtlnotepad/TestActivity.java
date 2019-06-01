package ua.leonidius.rtlnotepad;
import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import android.view.*;
import ua.leonidius.rtlnotepad.dialogs.*;
import java.util.*;
import ua.leonidius.rtlnotepad.utils.*;
import android.content.*;

public class TestActivity extends Activity 
{
	private boolean noEditorFragmentAdded = false;
	private NoEditorFragment noEditorFragment;
	public SharedPreferences pref;
	
	final int SIZE_SMALL = 14, SIZE_MEDIUM = 18, SIZE_LARGE = 22;
	final String PREF_TEXT_SIZE = "textSize", PREF_THEME = "theme", PREF_THEME_LIGHT = "light", PREF_THEME_DARK = "dark";
	
	public static TestActivity activity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null); // I'll manage to restore data on my own, thanks
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
		if (savedInstanceState != null) {
			restoreTabs(savedInstanceState);
		}
		
		// Opening a file from intent
		String path = null;
		try {
			path = getIntent().getData().getSchemeSpecificPart();
			if (path!=null) addTab(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		addNoEditorFragmentIfNeeded();
		
		LastFilesMaster.initSlots(this);
	}
	
	/**
	* Used for restoring tabs after activity is recreated (e.g. after a device has been rotated)
	**/
	private void restoreTabs(Bundle savedState) {
		for (TabData tabData : (LinkedHashSet<TabData>)savedState.getSerializable("tabs")) {
			ActionBar.Tab tab = getActionBar().newTab();
			tab.setText(tabData.name);
			EditorFragment fragment = new EditorFragment(tabData.text);
			fragment.file = tabData.file;
			fragment.mTag = tabData.tag;
			fragment.currentEncoding = tabData.encoding;
			fragment.hasUnsavedChanges = tabData.hasUnsavedChanges;
			tab.setTag(fragment);
			tab.setTabListener(new EditorTabListener());
			getActionBar().addTab(tab);
			if (tabData.isSelected) getActionBar().selectTab(tab);
		}
	}
	
	/**
	* Opening a file in a new tab
	**/
	private void addTab(File file) {
		// Detaching noEditorFragment
		removeNoEditorFragmentIfNeeded();
		
		// If the file is already opened, switching to the file's tab
		ActionBar.Tab fileTab = getFileTab(file);
		if (fileTab != null) {
			getActionBar().selectTab(fileTab);
			return;
		}
		
		// Creating a new tab
		ActionBar actionBar = getActionBar();
		ActionBar.Tab tab = actionBar.newTab();
		//tab.setCustomView(R.layout.tab_view);
		tab.setText(file.getName());
		//tab.setIcon(R.drawable.file);
		EditorFragment fragment = new EditorFragment(this, file);
		tab.setTag(fragment); // Adding fragment as a tag
		tab.setTabListener(new EditorTabListener());
		actionBar.addTab(tab);
		
		// Selecting the tab
		actionBar.selectTab(tab);
		
		LastFilesMaster.add(file);
	}
	
	// Adding a tab with blank editor for a new file
	private void addTab() {
		// Detaching noEditorFragment
		removeNoEditorFragmentIfNeeded();
		
		ActionBar actionBar = getActionBar();
		ActionBar.Tab tab = actionBar.newTab();
		tab.setText(R.string.new_document);
		//tab.setIcon(R.drawable.file);
		EditorFragment fragment = new EditorFragment(this);
		tab.setTag(fragment); // Adding fragment as a tag
		tab.setTabListener(new EditorTabListener());
		actionBar.addTab(tab);
		
		// Selecting the tab
		actionBar.selectTab(tab);
	}
	
	public void closeTab(ActionBar.Tab tab) {
		getActionBar().removeTab(tab);
		addNoEditorFragmentIfNeeded();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
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
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.options_open:
				OpenDialog openDialog = new OpenDialog(this);
				openDialog.setCallback(new OpenDialog.Callback() {
						@Override
						public void callback(File file)
						{
							addTab(file);
						}
				});
				openDialog.show(getFragmentManager(), "openDialog");
				return true;
			/*case R.id.options_save_as:
				if (getActionBar().getTabCount() == 0) break;
				openSaveDialog();
				return true;*/
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
				LastFilesDialog lfd = new LastFilesDialog(this, new LastFilesDialog.Callback(){
						@Override
						public void callback(String path)
						{
							addTab(new File(path));
						}
				});
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		for (int i = 0; i < getActionBar().getTabCount(); i++) {
			ActionBar.Tab tab = getActionBar().getTabAt(i);
			EditorFragment tabFragment = (EditorFragment)tab.getTag();
			if (tabFragment.hasUnsavedChanges) {
				new ExitDialog(this).show(getFragmentManager(), "exitDialog");
				return;
			}
		}
		super.onBackPressed();
	}
	
	/*private void openSaveDialog() {
		SaveDialog saveDialog = new SaveDialog(this);
		saveDialog.setCallback(new SaveDialog.Callback() {
				@Override
				public void callback(File file)
				{
					ActionBar.Tab selectedTab = getActionBar().getSelectedTab();
					selectedTab.setText(file.getName());
					EditorFragment fragment = (EditorFragment)selectedTab.getTag();
					fragment.file = file;
					fragment.setTextChanged(false);
					LastFilesMaster.add(file);
				}
			});
		saveDialog.show(getFragmentManager(), "saveDialog");
	}*/
	
	private void saveChanges() {
		EditorFragment selectedTabFragment = (EditorFragment)getActionBar().getSelectedTab().getTag();
		String text = selectedTabFragment.getEditor().getText().toString();
		String encoding = selectedTabFragment.currentEncoding;
		try
		{
			FileWorker.write(selectedTabFragment.file, text, encoding);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		selectedTabFragment.setTextChanged(false);
	}
	
	private ActionBar.Tab getFileTab(File file) {
		// Iterating over tabs
		for (int i = 0; i < getActionBar().getTabCount(); i++) {
			ActionBar.Tab tab = getActionBar().getTabAt(i);
			EditorFragment tabFragment = (EditorFragment)tab.getTag();
			if (tabFragment.file != null && tabFragment.file.equals(file)) {
				return tab;
			}
		}
		return null;
	}
	
	private void addNoEditorFragmentIfNeeded() {
		if (getActionBar().getTabCount() == 0) {
			FragmentTransaction sft = getFragmentManager().beginTransaction();
			if (!noEditorFragmentAdded) {
				noEditorFragment = new NoEditorFragment();
				sft.add(android.R.id.content, noEditorFragment, "noEditor");
				noEditorFragmentAdded = true;
			}
			sft.attach(noEditorFragment);
			sft.commitAllowingStateLoss();
		}
	}
	
	private void removeNoEditorFragmentIfNeeded() {
		if (getActionBar().getTabCount() == 0 && noEditorFragmentAdded) {
			FragmentTransaction sft = getFragmentManager().beginTransaction();
      		sft.detach(noEditorFragment);
        	sft.commit();
		}
	}
	
	private void setThemeNow (String theme, MenuItem item) {
		if (!item.isChecked()) {
			item.setChecked(true);
			SharedPreferences.Editor prefEdit = pref.edit();
			prefEdit.putString(PREF_THEME, theme);
			prefEdit.commit();
			/*if (theme.equals(PREF_THEME_LIGHT)) setTheme(R.style.Leonidius_Light);
			else setTheme(R.style.Leonidius_Dark);*/
			recreate();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		// Packing all data from tabs into TabData objects
		LinkedHashSet<TabData> tabs = new LinkedHashSet<TabData>();
		for (int i = 0; i < getActionBar().getTabCount(); i++) {
			ActionBar.Tab tab = getActionBar().getTabAt(i);
			TabData tabData = new TabData();
			tabData.name = tab.getText().toString();
			tabData.file = ((EditorFragment)tab.getTag()).file;
			tabData.text = ((EditorFragment)tab.getTag()).getEditor().getText().toString();
			tabData.encoding = ((EditorFragment)tab.getTag()).currentEncoding;
			tabData.hasUnsavedChanges = ((EditorFragment)tab.getTag()).hasUnsavedChanges;
			tabData.isSelected = (getActionBar().getSelectedTab() == tab);
			tabData.tag = ((EditorFragment)tab.getTag()).mTag;
			tabs.add(tabData);
		}
		outState.putSerializable("tabs", tabs);
		
		if (getActionBar().getTabCount() > 0) {
			outState.putInt("currentFragmentID", ((Fragment)getActionBar().getSelectedTab().getTag()).getId());
		} else {
			outState.putInt("currentFragmentID", noEditorFragment.getId());
		}
		
		LastFilesMaster.saveSlots(this); // Maybe I should've moved it to onDestroy()?
	}
	
	public static TestActivity getInstance() {
		return activity;
	}
	
	private void setTextSize(int size) {
		for (int i = 0; i < getActionBar().getTabCount(); i++) {
			ActionBar.Tab tab = getActionBar().getTabAt(i);
			EditorFragment fragment = (EditorFragment)tab.getTag();
			fragment.getEditor().setTextSize(size);
		}
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putInt(PREF_TEXT_SIZE, size);
		prefEditor.commit();
	}
	
}
