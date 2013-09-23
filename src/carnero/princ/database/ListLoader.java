package carnero.princ.database;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.model.Beer;
import carnero.princ.model.BeerAZComparator;
import com.github.kevinsawicki.http.HttpRequest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;

public class ListLoader extends AsyncTask<Integer, Void, ArrayList<Beer>> {

	private ILoadingStatusListener mStatusListener;
	private Helper mHelper;

	public ListLoader(Context context, ILoadingStatusListener listener) {
		mStatusListener = listener;
		mHelper = new Helper(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mStatusListener != null) {
			mStatusListener.onLoadingStart();
		}
	}

	@Override
	protected ArrayList<Beer> doInBackground(Integer... pubs) {
		if (pubs == null || pubs.length == 0) {
			return null;
		}

		ArrayList<Beer> list = new ArrayList<Beer>();
		for (int pub : pubs) {
			list.addAll(mHelper.loadBeers(pub, true));
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
}