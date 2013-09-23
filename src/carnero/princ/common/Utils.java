package carnero.princ.common;

import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	private static final Pattern tagsPattern = Pattern.compile("(<[^>]+>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern spacesPattern = Pattern.compile("(\\s+)", Pattern.CASE_INSENSITIVE);

	public static String convertStreamToString(InputStream stream) {
		if (stream == null) {
			return null;
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		String line;
		StringBuilder data = new StringBuilder();

		try {
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			Log.e(Constants.TAG, "Failed to read stream");
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(Constants.TAG, "Failed to close stream");
			}
		}

		return data.toString();
	}

	public static String stripTags(String text) {
		if (TextUtils.isEmpty(text)) {
			return "";
		}

		Matcher matcher;

		matcher = tagsPattern.matcher(text);
		text = matcher.replaceAll(" ");

		matcher = spacesPattern.matcher(text);
		text = matcher.replaceAll(" ");

		return text.trim();
	}

	public static String addLeadingZero(int number, int length) {
		StringBuilder builder = new StringBuilder();
		builder.append(number);

		while (builder.length() < length) {
			builder.insert(0, "0");
		}

		return builder.toString();
	}
}
