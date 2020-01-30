package ua.leonidius.rtlnotepad

import android.app.Application
import com.chibatching.kotpref.Kotpref
import ru.elifantiev.android.roboerrorreporter.RoboErrorReporter

class MyApplication : Application() {

    override fun onCreate() {
        RoboErrorReporter.bindReporter(this)
        Kotpref.init(this)
        super.onCreate()
    }

}