package carnero.princ.internet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.database.Structure;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.model.Beer;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.*;
import java.util.ArrayList;
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

		String[] lines = data.split("(</p[^>]*>)");
		String line;
		Beer beer;

		for (int i = 0; i < lines.length; i ++) {
			line = Utils.cleanString(lines[i]);

			if (!TextUtils.isEmpty(line)) {
				beer = new Beer();
				beer.pub = Structure.Table.PUB_PRINC;
				beer.current = true;
				beer.name = line;

				list.add(beer);
			}
		}

		return list;
	}
}