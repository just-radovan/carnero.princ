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
import carnero.princ.iface.IDownloadingStatusListener;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.model.*;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListDownloader extends AsyncTask<Void, Void, Void> {

	private IDownloadingStatusListener mStatusListener;
	private Gson mGson;
	private Helper mHelper;
	private SharedPreferences mPreferences;

	public ListDownloader(Context context, IDownloadingStatusListener listener) {
		mStatusListener = listener;
		mGson = new Gson();
		mHelper = new Helper(context);
		mPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mStatusListener != null) {
			mStatusListener.onDownloadingStart();
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		Def definition = downloadBreweries();
		ArrayList<Beer> list;

		int pub = mPreferences.getInt(Constants.PREF_PUB, Constants.LIST_PRINC.id);

		for (BeerList beerList : Constants.LIST) {
			long last = mPreferences.getLong(beerList.prefLastDownload, 0);
			if (beerList.id == pub && last > (System.currentTimeMillis() - Constants.DOWNLOAD_INTERVAL_SHORT)) {
				continue; // favorite pub
			} else if (beerList.id != pub && last > (System.currentTimeMillis() - Constants.DOWNLOAD_INTERVAL_LONG)) {
				continue; // other pub
			}

			list = downloadBeers(definition, beerList);
			if (list == null || list.isEmpty()) {
				continue;
			}

			mHelper.saveBeers(list, beerList);

			mPreferences.edit()
					.putLong(beerList.prefLastDownload, System.currentTimeMillis())
					.commit();
		}

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

		return null;
	}

	@Override
	protected void onPostExecute(Void nothing) {
		super.onPostExecute(nothing);

		if (mStatusListener != null) {
			mStatusListener.onDownloadingComplete();
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

	private ArrayList<Beer> downloadBeers(Def definition, BeerList beerList) {
		ArrayList<Beer> list = new ArrayList<Beer>();

		Log.d(Constants.TAG, "Downloading beer list from " + beerList.url + "...");

		String data;
		try {
			InputStream stream = HttpRequest.get(beerList.url).stream();
			data = Utils.convertStreamToString(stream, beerList);
			stream.close();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Failed to download beer list (" + e.getMessage() + ")");
			return null;
		}

		if (beerList.id == Constants.LIST_PRINC.id) {
			ArrayList<Beer> beers = PrincParser.parse(definition, data);
			if (beers != null) {
				list.addAll(beers);
			}
		} else if (beerList.id == Constants.LIST_ZLY.id) {
			ArrayList<Beer> beers = ZlyParser.parse(definition, data);
			if (beers != null) {
				list.addAll(beers);
			}
		} else if (beerList.id == Constants.LIST_PIVNICE.id) {
			ArrayList<Beer> beers = PivniceParser.parse(definition, data);
			if (beers != null) {
				list.addAll(beers);
			}
		}

		Log.d(Constants.TAG, "Beers found: " + list.size());

		return list;
	}
}