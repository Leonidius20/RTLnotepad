package ua.leonidius.navdialogs;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.*;

abstract class AdapterFactory {

    static SimpleAdapter getFileAdapter(Context context, File directory) {
        File[] unsortedFiles = directory.listFiles();
        TreeSet<File> filesSet = new TreeSet<>((File f1, File f2) -> {
            if (f1.isDirectory() && f2.isFile())
                return -1;
            else if (f2.isDirectory() && f1.isFile())
                return 1;
            else if (f1.isDirectory() && (!f2.isDirectory() && !f2.isFile()))
                return -1;
            else if (f2.isDirectory() && (!f1.isDirectory() && !f1.isFile()))
                return 1;
            else return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        });
        filesSet.addAll(Arrays.asList(unsortedFiles));
        Object[] files = filesSet.toArray();

        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> m;
        for (Object file : files) {
            m = new HashMap<>();
            m.put("name", ((File) file).getName());
            if (((File) file).isDirectory()) m.put("icon", R.drawable.folder);
            else m.put("icon", R.drawable.file);
            data.add(m);
        }
        String[] from = {"name", "icon"};
        int[] to = {R.id.listitem_text, R.id.listitem_icon};
        return new SimpleAdapter(context, data, R.layout.navdialogs_files_list_item, from, to);
    }

}