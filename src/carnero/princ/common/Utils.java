package carnero.princ.common;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	private static final Pattern tagsPattern = Pattern.compile("(<[^>]+>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern spacesPattern = Pattern.compile("(\\s+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern currentPattern = Pattern.compile("Aktuální nabídka", Pattern.CASE_INSENSITIVE);
	private static final Pattern queuePattern = Pattern.compile("V pořadí", Pattern.CASE_INSENSITIVE);
	private static final Pattern offerPattern = Pattern.compile("V nabídce [–|-]", Pattern.CASE_INSENSITIVE);
	private static final Pattern degreesPattern = Pattern.compile("( ?([0-9]+)° ?)", Pattern.CASE_INSENSITIVE);

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

	public static String cleanString(String text) {
		if (TextUtils.isEmpty(text)) {
			return "";
		}

		Matcher matcher;

		// remove tags
		matcher = tagsPattern.matcher(text);
		text = matcher.replaceAll(" ");

		// convert entities
		text = Html.fromHtml(text).toString();

		// remove abundant white chars
		matcher = spacesPattern.matcher(text);
		text = matcher.replaceAll(" ");

		// remove current offer
		matcher = currentPattern.matcher(text);
		if (matcher.find()) {
			return null;
		}

		// remove queued beers
		matcher = queuePattern.matcher(text);
		if (matcher.find()) {
			return null;
		}

		// remove special offer label
		matcher = offerPattern.matcher(text);
		text = matcher.replaceAll("");

		// format degrees
		matcher = degreesPattern.matcher(text);
		if (matcher.find()) {
			text = matcher.replaceAll(" " + matcher.group(2) + "° ");
		}

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

	// source: http://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
	public static double similarity(String s1, String s2) {
		if (s1.length() < s2.length()) { // s1 should always be bigger
			String swap = s1;
			s1 = s2;
			s2 = swap;
		}

		int bigLen = s1.length();
		if (bigLen == 0) {
			return 1.0; // empty strings, both of them
		} else {
			return (bigLen - computeEditDistance(s1, s2)) / (double) bigLen;
		}
	}

	private static int computeEditDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) {
					costs[j] = j;
				} else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}

			if (i > 0) {
				costs[s2.length()] = lastValue;
			}
		}

		return costs[s2.length()];
	}
}
