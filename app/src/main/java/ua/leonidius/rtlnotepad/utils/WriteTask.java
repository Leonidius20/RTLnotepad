package ua.leonidius.rtlnotepad.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Used for asynchronous writing of a string into a file. The result is returned via a callback
 * (false if failed to write, true otherwise).
 */
public class WriteTask extends AsyncTask<Void, Void, Boolean> {

    private File file;
    private String text;
    private String encoding;
    private Callback callback;

    public WriteTask(File file, String text, String encoding, Callback callback) {
        this.file = file;
        this.text = text;
        this.encoding = encoding;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            bw.write(text);
            bw.close();
            return true;
        } catch (Exception e) {
            Log.e("WriteTask RTLnotepad", e.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        callback.call(success);
    }

    public interface Callback {
        /**
         * Called when the writing process is finished.
         *
         * @param success true if writing succeeded, false otherwise
         */
        void call(boolean success);
    }

}