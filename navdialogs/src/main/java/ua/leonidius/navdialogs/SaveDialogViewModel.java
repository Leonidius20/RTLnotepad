package ua.leonidius.navdialogs;

import android.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;

public class SaveDialogViewModel extends ViewModel {
    private MutableLiveData<Pair<File, String>> fileData = new MutableLiveData<>();

    public MutableLiveData<Pair<File, String>> getFile() {
        return fileData;
    }

    void setData(File file, String encoding) {
        fileData.setValue(new Pair<>(file, encoding));
    }

}