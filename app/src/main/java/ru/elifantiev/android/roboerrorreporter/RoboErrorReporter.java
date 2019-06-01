package ru.elifantiev.android.roboerrorreporter;


import android.content.Context;

/**
 * Simple error reporting facility.
 * Saves stacktraces and exception information to external storage (if mounted and writable)
 * Files are saved to folder Android/data/your.package.name/files/stacktrace-dd-MM-YY.txt
 *
 * To apply error reporting simply do the following
 *   RoboErrorReporter.bindReporter(yourContext);
 */
public final class RoboErrorReporter {

    private RoboErrorReporter() {}

    /**
     * Apply error reporting to a specified application context
     * @param context context for which errors are reported (used to get package name)
     */
    public static void bindReporter(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(context));
    }

    public static void reportError(Context context, Throwable error) {
        ExceptionHandler.reportOnlyHandler(context).uncaughtException(Thread.currentThread(), error);
    }

}
