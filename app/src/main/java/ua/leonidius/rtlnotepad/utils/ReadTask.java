package ua.leonidius.rtlnotepad.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Used for asynchronous reading of a file into a string. The string is returned via
 * a callback. If failed to read the file, the resulting string would be null.
 */
public class ReadTask extends AsyncTask<Void, Void, String> {

    private File file;
    private String encoding;
    private Callback callback;

    public ReadTask(File file, String encoding, Callback callback) {
        this.file = file;
        this.encoding = encoding;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            StringBuilder sb = new StringBuilder();
            while (!isCancelled()) {
                String readLine = br.readLine();
                if (readLine == null) return sb.toString();
                if (sb.length() != 0) sb.append("\n");
                sb.append(readLine);
            }
        } catch (Exception e) {
            Log.e("ReadTask RTLnotepad", e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        callback.call(result);
    }

    public interface Callback {
        /**
         * Called when the reading process is finished.
         *
         * @param result Null if reading failed, read text otherwise
         */
        void call(String result);
    }

}