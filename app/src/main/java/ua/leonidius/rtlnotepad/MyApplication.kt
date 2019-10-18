package ua.leonidius.rtlnotepad

import android.app.Application
import ru.elifantiev.android.roboerrorreporter.RoboErrorReporter

class MyApplication : Application() {

    override fun onCreate() {
        RoboErrorReporter.bindReporter(this)
        super.onCreate()
    }

}