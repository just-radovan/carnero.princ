package carnero.princ.common;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import carnero.princ.model.BeerList;
import carnero.princ.model.Def;
import carnero.princ.model.DefBeer;
import carnero.princ.model.DefBrewery;

import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	private static final Pattern tagsPattern = Pattern.compile("(<[^>]+>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern spacesPattern = Pattern.compile("(\\s+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern currentPattern = Pattern.compile("Aktuální nabídka točených piv:?", Pattern.CASE_INSENSITIVE);
	private static final Pattern queuePattern = Pattern.compile("V pořadí", Pattern.CASE_INSENSITIVE);
	private static final Pattern offerPattern = Pattern.compile("V nabídce [–|-]", Pattern.CASE_INSENSITIVE);
	private static final Pattern degreesPattern = Pattern.compile("( ?([0-9]+)° ?)", Pattern.CASE_INSENSITIVE);
	//
	private static int sCleanLineCnt = -1;

	public static String convertStreamToString(InputStream stream, BeerList beerList) {
		if (stream == null) {
			return null;
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, beerList.encoding));
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

	public static String cleanString(String text, int lineCnt) {
		if (TextUtils.isEmpty(text) || (sCleanLineCnt > -1 && lineCnt > sCleanLineCnt)) {
			return null;
		} else {
			sCleanLineCnt = -1;
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

		// remove queued beers
		matcher = queuePattern.matcher(text);
		if (matcher.find()) {
			sCleanLineCnt = lineCnt;
			return null;
		}

		// remove current offer
		matcher = currentPattern.matcher(text);
		text = matcher.replaceAll("");

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

	public static void findBeer(Def definition, String line, ArrayList<Pair<String, String>> beers) {
		findBeer(definition, line, beers, false);
	}

	protected static void findBeer(Def definition, String line, ArrayList<Pair<String, String>> beers, boolean deep) {
		if (TextUtils.isEmpty(line) || definition == null || definition.map == null || beers == null) {
			return;
		}

		// strip diacritic
		String lineNormalized = Normalizer.normalize(line, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase();

		ArrayList<String> identifiers = new ArrayList<String>();
		identifiers.addAll(definition.map.keySet());
		Collections.sort(identifiers, new StringLengthComparator());

		Pair<DefBrewery, DefBeer> brewery;
		String beer;

		boolean found = false;
		for (String id : identifiers) {
			int index = lineNormalized.indexOf(id.toLowerCase());

			brewery = definition.map.get(id);
			if (index == 0) { // brewery is on the start
				if (brewery.second.removeID) {
					beer = line.substring(id.length()).trim();
				} else {
					beer = line.trim();
				}

				if (!TextUtils.isEmpty(beer)) {
					beers.add(new Pair(brewery.first.name, beer));
				}

				// try to find another beer
				findBeer(
						definition,
						line.substring(id.length()).trim(),
						beers,
						true
				);

				found = true;
				break;
			} else if (index > 0) { // brewery is not on the start
				String before;
				String after;
				boolean end = ((index + id.length()) >= line.length());

				if (end) { // brewery is on the end
					if (brewery.second.removeID) {
						beer = line.substring(0, index).trim();
					} else {
						beer = line.trim();
					}
					before = line.substring(0, index);
					after = null;
				} else { // brewery is in the middle
					if (brewery.second.removeID) {
						beer = line.substring(index + id.length()).trim();
					} else {
						beer = line.substring(index).trim();
					}
					before = line.substring(0, index);
					after = line.substring(index + id.length());
				}

				if (!TextUtils.isEmpty(beer)) {
					beers.add(new Pair(brewery.first.name, beer));
				}

				if (before != null) {
					before = before.trim();
				}
				if (after != null) {
					after = after.trim();
				}

				// try to find another beer
				if (!TextUtils.isEmpty(before)) {
					findBeer(
							definition,
							before,
							beers,
							true
					);
				}
				if (!TextUtils.isEmpty(after)) {
					findBeer(
							definition,
							after,
							beers,
							true
					);
				}

				found = true;
				break;
			}
		}

		if (!deep && !found) {
			beers.add(new Pair(null, line));
		}
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
