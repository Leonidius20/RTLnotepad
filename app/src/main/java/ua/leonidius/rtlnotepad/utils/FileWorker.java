package ua.leonidius.rtlnotepad.utils;
import java.io.*;

public abstract class FileWorker
{
	public static String read (File file, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String readLine = br.readLine();
			if (readLine == null) return sb.toString();
			if (sb.length() != 0) sb.append("\n");
			sb.append(readLine);
		}
	}
	
	public static void write (File file, String text, String encoding) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		bw.write(text);
		bw.close();
	}
	
	
	
}
