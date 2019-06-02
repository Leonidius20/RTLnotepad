package ua.leonidius.rtlnotepad;
import android.app.*;
import ru.elifantiev.android.roboerrorreporter.*;

public class MyApplication extends Application
{

	@Override
	public void onCreate()
	{
		RoboErrorReporter.bindReporter(this);
		super.onCreate();
	}

}
