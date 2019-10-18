package ua.leonidius.rtlnotepad;

import android.app.Application;
import ru.elifantiev.android.roboerrorreporter.RoboErrorReporter;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        RoboErrorReporter.bindReporter(this);
        super.onCreate();
    }

}