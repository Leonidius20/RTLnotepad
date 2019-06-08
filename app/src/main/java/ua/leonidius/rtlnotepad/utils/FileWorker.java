package ua.leonidius.rtlnotepad.utils;

import android.os.AsyncTask;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog;

import java.io.*;

public abstract class FileWorker
{
	private static LoadingDialog dialog = null;

	/**
	 * Used for asynchronous reading of a file into a string. The string is returned via
	 * a callback. If failed to read the file, the resulting string would be null.
	 * Shows a dialog with a progress bar.
	 */
	public static class ReadTask extends AsyncTask<Void, Void, String> {

		private File file;
		private String encoding;
		private Callback callback;

		public ReadTask(File file, String encoding, Callback callback) {
			this.file = file;
			this.encoding = encoding;
			this.callback = callback;
		}

		@Override
		protected void onPreExecute() {
			dialog = new LoadingDialog();
			dialog.show(MainActivity.getInstance().getFragmentManager(), "readingDialog");
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
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			dialog = null;
			callback.call(result);
		}

		@Override
		protected void onCancelled() {
			dialog.dismiss();
			dialog = null;
			callback.call(null);
		}

		public interface Callback {
			void call(String result);
		}

	}

	/**
	 * Used for asynchronous writing of a string into a file. The result is returned via a callback
	 * (false if failed to write, true if succeeded). Shows a dialog with a progress bar.
	 */
	public static class WriteTask extends AsyncTask<Void, Void, Boolean> {

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
		protected void onPreExecute() {
			dialog = new LoadingDialog();
			dialog.show(MainActivity.getInstance().getFragmentManager(), "writingDialog");
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
				bw.write(text);
				bw.close();
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			dialog.dismiss();
			dialog = null;
			callback.call(success);
		}

		public interface Callback {
			void call(boolean success);
		}

	}

}