package carnero.princ.internet;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.iface.IDownloadStatusListener;
import carnero.princ.model.Beer;
import carnero.princ.model.BeerAZComparator;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListDownloader extends AsyncTask<Void, Void, ArrayList<Beer>> {

	private Context mContext;
	private IDownloadStatusListener mStatusListener;
	//
	private Pattern mTablePattern = Pattern.compile("<table[^>]*>[^<]*<tbody[^>]*>(.*?)</tbody>[^<]*</table>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private Pattern mBeersPattern = Pattern.compile("<tr[^>]*>[^<]*<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE);

	public ListDownloader(Context context, IDownloadStatusListener listener) {
		mContext = context;
		mStatusListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mStatusListener != null) {
			mStatusListener.onDownloadStarted();
		}
	}

	@Override
	protected ArrayList<Beer> doInBackground(Void... params) {
		InputStream stream = null;
		try {
			stream = HttpRequest.get(Constants.LIST_URL).stream();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Failed to download beer list");
			e.printStackTrace();
		}

		String response = Utils.convertStreamToString(stream);
		ArrayList<Beer> list = parsePage(response);

		// TODO: store data

		return list;
	}

	@Override
	protected void onPostExecute(ArrayList<Beer> list) {
		super.onPostExecute(list);

		if (mStatusListener != null) {
			mStatusListener.onDownloadCompleted(list);
		}
	}

	private ArrayList<Beer> parsePage(String data) {
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
			line = Utils.stripTags(lines[i]);

			if (!TextUtils.isEmpty(line)) {
				beer = new Beer();
				beer.current = true;
				beer.name = Html.fromHtml(line).toString();

				list.add(beer);
			}
		}

		Collections.sort(list, new BeerAZComparator());

		return list;
	}
}