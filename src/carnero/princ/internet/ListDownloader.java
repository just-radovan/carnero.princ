package carnero.princ.internet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.data.Breweries;
import carnero.princ.data.Brewery;
import carnero.princ.database.Helper;
import carnero.princ.database.Structure;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.model.Beer;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListDownloader extends AsyncTask<Void, Void, ArrayList<Beer>> {

	private Context mContext;
	private ILoadingStatusListener mStatusListener;
	private Helper mHelper;
	private SharedPreferences mPreferences;
	// patterns
	private Pattern mTablePattern = Pattern.compile("<table[^>]*>[^<]*<tbody[^>]*>(.*?)</tbody>[^<]*</table>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private Pattern mBeersPattern = Pattern.compile("<tr[^>]*>[^<]*<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE);

	public ListDownloader(Context context, ILoadingStatusListener listener) {
		mContext = context;
		mStatusListener = listener;
		mHelper = new Helper(context);
		mPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mStatusListener != null) {
			mStatusListener.onLoadingStart();
		}
	}

	@Override
	protected ArrayList<Beer> doInBackground(Void... params) {
		long last = mPreferences.getLong(Constants.PREF_LAST_DOWNLOAD, 0);
		if (last > (System.currentTimeMillis() - (30 * 60 * 1000))) { // 30 mins
			return null;
		}

		Log.d(Constants.TAG, "Downloading beer list from " + Constants.LIST_URL_PRINC + "...");

		InputStream stream = null;
		try {
			stream = HttpRequest.get(Constants.LIST_URL_PRINC).stream();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Failed to download beer list");
		}

		String response = Utils.convertStreamToString(stream);
		ArrayList<Beer> list = parsePagePrinc(response);

		Log.d(Constants.TAG, "Beers found: " + list.size());

		mHelper.saveBeers(list, Structure.Table.PUB_PRINC);

		mPreferences.edit()
				.putLong(Constants.PREF_LAST_DOWNLOAD, System.currentTimeMillis())
				.commit();

		return list;
	}

	@Override
	protected void onPostExecute(ArrayList<Beer> list) {
		super.onPostExecute(list);

		if (mStatusListener != null) {
			mStatusListener.onLoadingComplete(list);
		}
	}

	private ArrayList<Beer> parsePagePrinc(String data) {
		if (TextUtils.isEmpty(data)) {
			return null;
		}

		ArrayList<Beer> list = new ArrayList<Beer>();
		Matcher matcher;

		matcher = mTablePattern.matcher(data);
		if (matcher.find() && matcher.groupCount() > 0) {
			data = matcher.group(1);
		}

		matcher = mBeersPattern.matcher(data);
		if (matcher.find() && matcher.groupCount() > 0) {
			data = matcher.group(1);
		}

		String[] lines = data.split("(</p[^>]*>|<br[^>]*>)");
		Beer beer;

		for (String line : lines) {
			// clean string
			line = Utils.cleanString(line);
			if (TextUtils.isEmpty(line)) {
				continue;
			}

			ArrayList<Pair<String, String>> filtered = new ArrayList<Pair<String, String>>();
			findBeer(line, filtered);

			// parse brewery and save beers
			for (Pair<String, String> item : filtered) {
				beer = new Beer();
				beer.pub = Structure.Table.PUB_PRINC;
				beer.current = true;
				beer.brewery = item.first;
				beer.name = item.second;

				list.add(beer);
			}
		}

		return list;
	}

	protected void findBeer(String line, ArrayList<Pair<String, String>> beers) {
		findBeer(line, beers, false);
	}

	protected void findBeer(String line, ArrayList<Pair<String, String>> beers, boolean deep) {
		if (TextUtils.isEmpty(line) || beers == null) {
			return;
		}

		// strip diacritic
		String lineNormalized = Normalizer.normalize(line, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase();

		Set<String> breweries = Breweries.map.keySet();
		Brewery brewery;
		String beer;

		boolean found = false;
		for (String item : breweries) {
			brewery = Breweries.map.get(item);

			int index = lineNormalized.indexOf(brewery.identifier.toLowerCase());
			if (index == 0) { // brewery is on the start
				if (brewery.removeID) {
					beer = line.substring(brewery.identifier.length()).trim();
				} else {
					beer = line;
				}

				beers.add(new Pair(brewery.name, beer));

				// try to find another beer
				findBeer(line.substring(brewery.identifier.length()).trim(), beers, true);

				found = true;
				break;
			} else if (index > 0) { // brewery is on the end
				if (brewery.removeID) {
					beer = line.substring(0, index).trim();
				} else {
					beer = line;
				}

				beers.add(new Pair(brewery.name, beer));

				// try to find another beer
				findBeer(line.substring(0, index).trim(), beers, true);
				findBeer(line.substring(index + brewery.identifier.length()).trim(), beers, true);

				found = true;
				break;
			}
		}

		if (!deep && !found) {
			beers.add(new Pair(null, line));
		}
	}
}