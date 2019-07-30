package ua.leonidius.navdialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import androidx.fragment.app.DialogFragment;

import java.io.File;

/**
 * This class is a base for OpenDialog and SaveDialog.
 */
abstract class NavigationDialog extends DialogFragment implements OnItemClickListener {

    private TextView pathView;
    private ListView filesList;
    protected File currentDir = null;

    public static final String BUNDLE_CURRENT_DIR = "currentDir";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                currentDir = (File)getArguments().getSerializable(BUNDLE_CURRENT_DIR);
            }
        } else {
            currentDir = (File)savedInstanceState.getSerializable(BUNDLE_CURRENT_DIR);
        }
        if (currentDir == null) currentDir = Environment.getExternalStorageDirectory();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        setMaxHeight(dialog);
        return dialog;
    }

    void setMaxHeight(Dialog dialog) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
    }

    protected void initView(View dialogView, Bundle savedState) {
        pathView = dialogView.findViewById(R.id.pathview);
        filesList = dialogView.findViewById(R.id.listview);
        filesList.setOnItemClickListener(this);
        View header = getActivity().getLayoutInflater().inflate(R.layout.navdialogs_files_list_item, null, false);
        ImageView imageView = header.findViewById(R.id.listitem_icon);
        imageView.setImageResource(R.drawable.up);
        TextView headerText = header.findViewById(R.id.listitem_text);
        headerText.setText(R.string.up);
        filesList.addHeaderView(header);
    }

    @Override
    public void onItemClick(AdapterView<?> p1, View item, int position, long p4) {
        if (position == 0) {
            up();
            return;
        }
        String path;
        String name = ((TextView) item.findViewById(R.id.listitem_text)).getText().toString();
        if (currentDir.getPath().equals("/")) path = currentDir.getPath() + name;
        else path = currentDir.getPath() + "/" + name;
        File file = new File(path);
        if (file.isDirectory()) openDir(file);
        else onFileClick(file);
    }

    protected void onFileClick(File file) {
    }

    protected void openDir(File directory) {
        try {
            filesList.setAdapter(AdapterFactory.getFileAdapter(getActivity(), directory));
            currentDir = directory;
            pathView.setText(currentDir.getPath());
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.folder_open_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void up() {
        if (currentDir.getPath().equals("/")) return;
        currentDir = currentDir.getParentFile();
        openDir(currentDir);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_CURRENT_DIR, currentDir);
    }

}