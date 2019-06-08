// A base for OpenDialog and SaveDialog
package ua.leonidius.rtlnotepad.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.R;
import ua.leonidius.rtlnotepad.utils.AdapterFactory;

import java.io.File;

public abstract class NavigationDialog extends DialogFragment implements OnItemClickListener
{
	private TextView pathView;
	private ListView filesList;
	protected File currentDir;
	protected MainActivity activity;
	
	public NavigationDialog(MainActivity activity) {
		super();
		this.activity = activity;
	}
	
	protected void initView(View dialogView) {
		pathView = dialogView.findViewById(R.id.pathview);
		filesList = dialogView.findViewById(R.id.listview);
		filesList.setOnItemClickListener(this);
		View header = activity.getLayoutInflater().inflate(R.layout.list_item, null);
		ImageView imageView = header.findViewById(R.id.listitem_icon);
		imageView.setImageResource(R.drawable.up);
		TextView headerText = header.findViewById(R.id.listitem_text);
		headerText.setText(R.string.up);
		filesList.addHeaderView(header);
	}
	
	// Opening a directory after creating a dialog
	protected void onCreateOpenDir(Bundle savedInstanceState) {
		if (savedInstanceState==null) openDir(Environment.getExternalStorageDirectory());
		else openDir(currentDir);
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View item, int position, long p4)
	{
		if (position == 0) {
			up();
			return;
		}
		String path;
		String name = ((TextView)item.findViewById(R.id.listitem_text)).getText().toString();
		if (currentDir.getPath().equals("/")) path = currentDir.getPath()+name;
		else path = currentDir.getPath()+"/"+name;
		File file = new File(path);
		if (file.isDirectory()) openDir(file);
		else onFileClick(file);
	}

	protected void onFileClick(File file) {}
	
	protected void openDir(File directory) {
		try {
			filesList.setAdapter(AdapterFactory.getFileAdapter(directory, activity));
			currentDir = directory;
			pathView.setText(currentDir.getPath());
		} catch (Exception e) {
			Toast.makeText(activity, R.string.folder_open_error, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void up() {
		if (currentDir.getPath().equals("/")) return;
		currentDir = currentDir.getParentFile();
		openDir(currentDir);
	}
	
}
