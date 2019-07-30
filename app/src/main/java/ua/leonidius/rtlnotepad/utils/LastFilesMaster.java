package ua.leonidius.rtlnotepad.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.SimpleAdapter;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
* This class manages the list of last opened files.
**/

public abstract class LastFilesMaster
{
	private static LinkedList<File> slots; // stores files' paths
	private static final String[] slot_names = {"slot1", "slot2", "slot3", "slot4", "slot5"};
	private static final String EMPTY = "empty";
	private static final int SLOTS_SIZE = 5;
	
	public static void add (File file) {
		// Checking if file is already on the list and placing it to the top in this case
		for (File fileInSlot : slots) {
			if (fileInSlot == file) {
				slots.remove(fileInSlot);
				slots.addFirst(file);
				return;
			}
		}

		if (slots.size() == SLOTS_SIZE) slots.removeLast();
		slots.addFirst(file);
	}
	
	// Retrieves recent files saved in Preferences. Called in main activity's onCreate()
	public static void initSlots(MainActivity activity) {
		slots = new LinkedList<>();
		String path;
		File file;
		for (int i = 0; i < SLOTS_SIZE; i++) {
			path = activity.pref.getString(slot_names[i], EMPTY);
			if (!path.equals(EMPTY)) {
				file = new File(path);
				if (file.exists()) slots.add(file);
				else slots.add(null);
			} else slots.add(null);
		}
	}
	
	// Saves recent files to the preferences. Called in main activity's onSaveInstanceState()
	public static void saveSlots(MainActivity activity) {
		SharedPreferences.Editor edit = activity.pref.edit();
		for (int i = 0; i < SLOTS_SIZE; i++) {
			if (slots.get(i) == null || (!slots.get(i).exists())) {
				edit.putString(slot_names[i], EMPTY);
				continue;
			}
			try {
				edit.putString(slot_names[i], slots.get(i).getCanonicalPath());
			} catch (IOException e) {
				edit.putString(slot_names[i], EMPTY);
			}
		}
		edit.apply();
	}
	
	// Returns an adapter for last files list for LastFilesDialog
	public static SimpleAdapter getAdapter(Activity activity) {
		ArrayList<Map<String, Object>> data = new ArrayList<>();
		Map<String, Object> m;
		for (File file : slots) {
			if (file != null) {
				m = new HashMap<>();
				m.put("name", file.getName());
				m.put("path", file.getPath());
				m.put("icon", R.drawable.file);
				data.add(m);
			}
		}
		String[] from = {"name", "icon", "path"};
		int[] to = {R.id.lastFilesItem_name, R.id.lastFilesItem_image, R.id.lastFilesItem_path};
		return new SimpleAdapter(activity, data, R.layout.last_files_item, from, to);
	}

}