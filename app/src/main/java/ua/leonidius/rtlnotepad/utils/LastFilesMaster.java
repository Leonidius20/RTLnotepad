package ua.leonidius.rtlnotepad.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.SimpleAdapter;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* This class manages the list of last opened files.
* Author: Leonidius20
* Project: RTLnotepad
**/

public abstract class LastFilesMaster
{
	private static File[] slots = new File[5];
	private static String[] slot_names = {"slot1", "slot2", "slot3", "slot4", "slot5"};
	protected static String EMPTY = "empty";
	
	public static void add (File file) {
		// Checking if file is already on the list
		for (int i = 0; i < slots.length; i++) {
			// If the file is already in the list, we move it to the top of the list
			// (more recent files are higher in the list)
			if (slots[i] != null && slots[i].getAbsolutePath().equals(file.getAbsolutePath())) {
				moveFileUp(i, file);
				return;
			}
		}
		
		// If the file wasn't on the list, we move all of the files
		// in the list one position down to free one place at the top.
		for (int i = slots.length - 2; i > -1; i--) {
			slots[i + 1] = slots[i];
		}
		slots[0] = file;
	}
	
	// Moves the specified file to the top of the list, moving
	// other files on the way.
	private static void moveFileUp (int currentPosition, File file) {
		/*for (int i = currentPosition; i < slots.length; i++) {
			if (i < (slots.length - 1)) {
				slots[i] = slots[i+1];
			} else {
				slots[i] = file;
			}
		}*/
		for (int i = 0; i < currentPosition; i++) {
			File higherFile = slots[currentPosition - 1];
			slots[currentPosition - 1] = file;
			slots[currentPosition] = higherFile;
		}
	}
	
	// Gets recent files saved in Preferences. Called in the onCreate() of the main activity
	public static void initSlots (MainActivity activity) {
		String path;
		File file;
		for (int i = 0; i < slots.length; i++) {
			path = activity.pref.getString(slot_names[i], EMPTY);
			if (!path.equals(EMPTY)) {
				file = new File(path);
				if (file.exists()) slots[i] = file;
			}
		}
	}
	
	// Saves recent files to the preferences. Called in main activity's onSaveInstanceState()
	public static void saveSlots (MainActivity context) {
		SharedPreferences.Editor edit = context.pref.edit();
		for (int i=0; i<slots.length; i++) {
			if (slots[i] == null || (!slots[i].exists())) edit.putString(slot_names[i], EMPTY);
			else edit.putString(slot_names[i], slots[i].toString());
		}
		edit.commit();
	}
	
	// Returns an adapter for last files list for LastFilesDialog
	public static SimpleAdapter getAdapter(Activity activity) {
		ArrayList <Map<String, Object>> data = new ArrayList <Map<String, Object>>();
		Map <String, Object> m;
		for (File file : slots) {
			if (file != null) {
				m = new HashMap<String, Object>();
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
