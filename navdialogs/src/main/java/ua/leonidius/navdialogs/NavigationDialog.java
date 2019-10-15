package ua.leonidius.navdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;

import java.io.File;

/**
 * This class is a base for OpenDialog and SaveDialog.
 */
abstract class NavigationDialog extends DialogFragment implements OnItemClickListener {

    private TextView pathView;
    private ListView filesList;
    //File currentDir = null;
    NavDialogViewModel viewModel;

    public static final String BUNDLE_CURRENT_DIR = "currentDir";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (savedInstanceState == null) {
            if (getArguments() != null) {
                getViewModel().currentDir = (File)getArguments().getSerializable(BUNDLE_CURRENT_DIR);
            }
        } else {
            getViewModel().currentDir = ((File)savedInstanceState.getSerializable(BUNDLE_CURRENT_DIR));
        }
        if (getViewModel().currentDir == null) getViewModel().currentDir = (Environment.getExternalStorageDirectory());*/
    }

    @NonNull
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
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.navdialogs_files_list_item, null, false);
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
        if (getViewModel().currentDir.getPath().equals("/")) path = getViewModel().currentDir.getPath() + name;
        else path = getViewModel().currentDir.getPath() + "/" + name;
        File file = new File(path);
        if (file.isDirectory()) openDir(file);
        else onFileClick(file);
    }

    protected void onFileClick(File file) {
    }

    void openDir(File directory) {
        try {
            filesList.setAdapter(AdapterFactory.getFileAdapter(getActivity(), directory));
            getViewModel().currentDir = directory;
            pathView.setText(getViewModel().currentDir.getPath());
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.folder_open_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void up() {
        if (getViewModel().currentDir.getPath().equals("/")) return;
        getViewModel().currentDir = getViewModel().currentDir.getParentFile();
        openDir(getViewModel().currentDir);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable(BUNDLE_CURRENT_DIR, currentDir);
    }

    public abstract NavDialogViewModel getViewModel();

    // SUGGESTION:
    /*public abstract Class getViewModelClass();
    public <T> getViewModel() {
        return ViewModelProviders.of(getActivity()).get(getViewModelClass());
    }*/
    abstract static class NavDialogViewModel extends ViewModel {
        File currentDir;
    }

}