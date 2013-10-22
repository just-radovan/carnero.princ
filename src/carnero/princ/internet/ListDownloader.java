package carnero.princ.internet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.database.Structure;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.model.*;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListDownloader extends AsyncTask<Void, Void, ArrayList<Beer>> {

	private Context mContext;
	private ILoadingStatusListener mStatusListener;
	private Gson mGson;
	private Helper mHelper;
	private SharedPreferences mPreferences;
	// patterns
	private Pattern mTablePattern = Pattern.compile("<table[^>]*>[^<]*<tbody[^>]*>(.*?)</tbody>[^<]*</table>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private Pattern mBeersPattern = Pattern.compile("<tr[^>]*>[^<]*<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE);

	public ListDownloader(Context context, ILoadingStatusListener listener) {
		mContext = context;
		mStatusListener = listener;
		mGson = new Gson();
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

		Def definition = downloadBreweries();
		ArrayList<Beer> list = downloadBeers(definition);
		if (list == null) {
			return null;
		}

		mHelper.saveBeers(list, Constants.PUB_PRINC);

		mPreferences.edit()
				.putLong(Constants.PREF_LAST_DOWNLOAD, System.currentTimeMillis())
				.commit();

		// check already saved beers
		ArrayList<BeerName> orphans = mHelper.loadUnknownBeers();
		ArrayList<Pair<String, String>> discoveries = new ArrayList<Pair<String, String>>();
		for (BeerName orphan : orphans) {
			discoveries.clear();

			Utils.findBeer(definition, orphan.name, discoveries);

			int interesting = 0;
			for (Pair<String, String> discovery : discoveries) {
				if (!TextUtils.isEmpty(discovery.first)) {
					if (interesting == 0) { // update original
						orphan.brewery = discovery.first;
						orphan.name = discovery.second;

						mHelper.updateBeerBrewery(orphan);
					} else { // add new one with values of original & updated brewery, name
						Beer beer = mHelper.loadBeer(orphan.id);

						beer.id = 0;
						beer.brewery = discovery.first;
						beer.name = discovery.second;

						mHelper.saveBeer(beer);
					}

					interesting++;
				}
			}
		}

		return list;
	}

	@Override
	protected void onPostExecute(ArrayList<Beer> list) {
		super.onPostExecute(list);

		if (mStatusListener != null) {
			mStatusListener.onLoadingComplete(list);
		}
	}

	private Def downloadBreweries() {
		Log.d(Constants.TAG, "Downloading breweries...");

		Def definition;
		try {
			InputStream stream = HttpRequest.get(Constants.LIST_URL_BREWERIES).stream();
			Reader reader = new InputStreamReader(stream);
			definition = mGson.fromJson(reader, Def.class);
			stream.close();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Failed to download breweries (" + e.getMessage() + ")");
			return null;
		}

		for (DefBrewery brewery : definition.breweries) {
			for (DefBeer beer : brewery.beers) {
				definition.map.put(beer.identifier, new Pair(brewery, beer));
			}
		}

		Log.d(Constants.TAG, "Breweries found: " + definition.breweries.size());

		return definition;
	}

	private ArrayList<Beer> downloadBeers(Def definition) {
		Log.d(Constants.TAG, "Downloading beer list from " + Constants.LIST_URL_PRINC + "...");

		String data;
		try {
			InputStream stream = HttpRequest.get(Constants.LIST_URL_PRINC).stream();
			data = Utils.convertStreamToString(stream);
			stream.close();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Failed to download beer list (" + e.getMessage() + ")");
			return null;
		}

		ArrayList<Beer> list = parsePagePrinc(definition, data);

		Log.d(Constants.TAG, "Beers found: " + list.size());

		return list;
	}

	private ArrayList<Beer> parsePagePrinc(Def definition, String data) {
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
		int lineCnt = 0;

		for (String line : lines) {
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
				beer.pub = Constants.PUB_PRINC;
				beer.current = true;
				beer.brewery = item.first;
				beer.name = item.second;

				list.add(beer);
			}
		}

		return list;
	}
}