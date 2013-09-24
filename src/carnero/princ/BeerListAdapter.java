package carnero.princ;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.model.Beer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BeerListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Beer> mList;
	private DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public BeerListAdapter(Context context) {
		mContext = context;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item_beer_card, parent, false);
		}

		Beer beer = getItem(position);

		TextView vName = (TextView) view.findViewById(R.id.beer_name);
		TextView vTapPrev = (TextView) view.findViewById(R.id.beer_tap_prev);
		TextView vTapSince = (TextView) view.findViewById(R.id.beer_tap_since);

		// view.findViewById(R.id.beer_search).setOnClickListener(new SearchClickListener(beer));
		vName.setText(beer.name);
		if (beer.onTapPrevious > 0) {
			vTapPrev.setText(mDateFormat.format(new Date(beer.onTapPrevious)));
		} else {
			vTapPrev.setText("poprvé na čepu");
		}
		vTapSince.setText(mDateFormat.format(new Date(beer.onTapSince)));

		return view;
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return mList.size();
		}
	}

	@Override
	public Beer getItem(int position) {
		if (mList == null) {
			return null;
		} else {
			return mList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		if (mList == null) {
			return 0;
		} else {
			return mList.get(position).id;
		}
	}

	public void setData(ArrayList<Beer> list) {
		mList = list;

		notifyDataSetChanged();
	}

	// classess

	public class SearchClickListener implements View.OnClickListener {

		private Beer mBeer;

		public SearchClickListener(Beer beer) {
			mBeer = beer;
		}

		@Override
		public void onClick(View view) {
			try {
				String nameEncoded = URLEncoder.encode(mBeer.name, "utf-8");
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Constants.UNTAPPD_URL, nameEncoded)));
				mContext.startActivity(intent);
			} catch (UnsupportedEncodingException uee) {
				Log.e(Constants.TAG, "Failed to encode beer's name");
			}
		}
	}
}
