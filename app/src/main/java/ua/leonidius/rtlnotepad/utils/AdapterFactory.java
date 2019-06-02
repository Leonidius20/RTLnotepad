package ua.leonidius.rtlnotepad.utils;
import android.widget.*;
import java.io.*;
import java.util.*;
import ua.leonidius.rtlnotepad.*;
import android.app.*;

public abstract class AdapterFactory
{
	public static SimpleAdapter getFileAdapter (File directory, Activity activity) {
		File[] unsortedFiles = directory.listFiles();
		TreeSet<File> filesSet = new TreeSet<File>(new Comparator<File>(){
				public int compare(File f1, File f2)
				{
					if (f1.isDirectory() && f2.isFile())
						return -1;
					else if(f2.isDirectory() && f1.isFile())
						return 1;
					else if (f1.isDirectory() && (!f2.isDirectory()&&!f2.isFile()))
						return -1;
					else if (f2.isDirectory() && (!f1.isDirectory()&&!f1.isFile()))
						return 1;
					else return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
				}
		});
		filesSet.addAll(Arrays.asList(unsortedFiles));
		Object[] files = filesSet.toArray();

		ArrayList <Map<String, Object>> data = new ArrayList <Map<String, Object>>();
		Map <String, Object> m;
		for (Object file : files) {
			m = new HashMap<String, Object>();
			m.put("name", ((File) file).getName());
			if (((File) file).isDirectory()) m.put("icon", R.drawable.folder);
			else m.put("icon", R.drawable.file);
			data.add(m);
		}
		String[] from = {"name", "icon"};
		int[] to = {R.id.listitem_text, R.id.listitem_icon};
		return new SimpleAdapter(activity, data, R.layout.list_item, from, to);
	}
	
}
