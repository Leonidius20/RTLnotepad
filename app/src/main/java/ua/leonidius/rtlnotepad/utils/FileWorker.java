package ua.leonidius.rtlnotepad.utils;

import android.os.AsyncTask;
import ua.leonidius.rtlnotepad.MainActivity;
import ua.leonidius.rtlnotepad.dialogs.LoadingDialog;

import java.io.*;

public abstract class FileWorker
{
	private static LoadingDialog dialog = null;

	/**
	 * Writes a file to the disk using FileWorker.WriteTask
	 * @param file File to write into
	 * @param text Text to write into the file
	 * @param encoding Encoding to use
	 * @param callback Defines what to do after the writing.
	 */
	public static void writeFile(File file, String text, String encoding, WriteCallback callback) {
		FileWorker.WriteTask task = new FileWorker.WriteTask(file, text, encoding, callback::call);
		task.execute();
	}

	public interface WriteCallback {
		void call(boolean success);
	}

	/**
	 * Reads a specified file and sets its contents as a text to the editor.
	 * Shows a Toast if the reading fails.
	 * @param file File to read
	 * @param encoding Encoding to use for decoding of the file
	 */
	public static void readFile(File file, String encoding, ReadCallback callback) {
		/*FileWorker.ReadTask task = new FileWorker.ReadTask(file, encoding, result -> {
			if (result == null) {
				Toast.makeText(mActivity, R.string.reading_error, Toast.LENGTH_SHORT).show();
				callback.call(false);
			} else {
				editor.setText(result); // Takes a lot of time for big texts. Most of it
				setTextChanged(false);
				callback.call(true);
			}
		});*/
		FileWorker.ReadTask task = new FileWorker.ReadTask(file, encoding, callback::call);
		task.execute();
	}

	public interface ReadCallback {
		void call(boolean success, String result);
	}

	/**
	 * Used for asynchronous reading of a file into a string. The string is returned via
	 * a callback. If failed to read the file, the resulting string would be null.
	 * Shows a dialog with a progress bar.
	 */
	private static class ReadTask extends AsyncTask<Void, Void, String> {

		private File file;
		private String encoding;
		private Callback callback;

		ReadTask(File file, String encoding, Callback callback) {
			this.file = file;
			this.encoding = encoding;
			this.callback = callback;
		}

		@Override
		protected void onPreExecute() {
			dialog = new LoadingDialog();
			dialog.show(MainActivity.getInstance().getSupportFragmentManager(), "readingDialog");
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
			callback.call(result != null, result);
		}

		@Override
		protected void onCancelled() {
			dialog.dismiss();
			dialog = null;
			// TODO: notify that it was cancelled
			callback.call(false, null);
		}

		public interface Callback {
			void call(boolean success, String result);
		}

	}

	/**
	 * Used for asynchronous writing of a string into a file. The result is returned via a callback
	 * (false if failed to write, true if succeeded). Shows a dialog with a progress bar.
	 */
	private static class WriteTask extends AsyncTask<Void, Void, Boolean> {

		private File file;
		private String text;
		private String encoding;
		private Callback callback;

		WriteTask(File file, String text, String encoding, Callback callback) {
			this.file = file;
			this.text = text;
			this.encoding = encoding;
			this.callback = callback;
		}

		@Override
		protected void onPreExecute() {
			dialog = new LoadingDialog();
			dialog.show(MainActivity.getInstance().getSupportFragmentManager(), "writingDialog");
			// TODO: show dialog in EditorFragment, dismiss on callback
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