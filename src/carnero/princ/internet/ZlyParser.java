package carnero.princ.internet;

import android.text.TextUtils;
import android.util.Pair;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.model.Beer;
import carnero.princ.model.Def;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZlyParser {

	private static Pattern sTablePattern = Pattern.compile("<table[^>]*>(.*?)</table>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern sBeersPattern = Pattern.compile("<td width=\"200\">(.*?)</td>", Pattern.CASE_INSENSITIVE);

	public static ArrayList<Beer> parse(Def definition, String data) {
		if (TextUtils.isEmpty(data)) {
			return null;
		}

		ArrayList<Beer> list = new ArrayList<Beer>();
		Matcher matcher;

		matcher = sTablePattern.matcher(data);
		if (matcher.find() && matcher.groupCount() > 0) {
			data = matcher.group(1);
		}

		String line;
		int lineCnt = 0;
		Beer beer;

		matcher = sBeersPattern.matcher(data);
		while (matcher.find()) {
			if (matcher.groupCount() <= 0) {
				continue;
			}
			line = matcher.group(1);

			// clean string
			line = Utils.cleanString(line, lineCnt);

			lineCnt++;
			if (TextUtils.isEmpty(line)) {
				continue;
			}

			ArrayList<Pair<String, String>> filtered = new ArrayList<Pair<String, String>>();
			Utils.findBeer(definition, line, filtered);

			// parse brewery and save beers
			for (Pair<String, String> item : filtered) {
				beer = new Beer();
				beer.pub = Constants.LIST_ZLY.id;
				beer.current = true;
				beer.brewery = item.first;
				beer.name = item.second;

				list.add(beer);
			}
		}

		return list;
	}
}
