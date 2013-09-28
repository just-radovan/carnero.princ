package carnero.princ;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.data.Breweries;
import carnero.princ.data.Brewery;
import carnero.princ.model.Beer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

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

		View vContainer = view.findViewById(R.id.container);
		View vPaddingT = view.findViewById(R.id.padding_top);
		View vPaddingB = view.findViewById(R.id.padding_bottom);
		View vSeparator = view.findViewById(R.id.separator);
		TextView vBrewery = (TextView) view.findViewById(R.id.brewery_name);
		TextView vName = (TextView) view.findViewById(R.id.beer_name);
		TextView vTapPrev = (TextView) view.findViewById(R.id.beer_tap_prev);
		TextView vTapSince = (TextView) view.findViewById(R.id.beer_tap_since);

		Beer beerPrev = getItem(position - 1);
		Beer beerNext = getItem(position + 1);
		boolean before = (beerPrev != null && ((beerPrev.brewery == null && beer.brewery == null) || (beerPrev.brewery.equalsIgnoreCase(beer.brewery))));
		boolean after = (beerNext != null && ((beerNext.brewery == null && beer.brewery == null) || (beerNext.brewery.equalsIgnoreCase(beer.brewery))));

		if (!TextUtils.isEmpty(beer.brewery)) {
			vBrewery.setText(beer.brewery);
		} else {
			vBrewery.setText("N/A");
		}

		if (before && after) {
			vContainer.setBackgroundResource(R.drawable.bg_card_middle);
			vPaddingT.setVisibility(View.GONE);
			vPaddingB.setVisibility(View.GONE);
			vSeparator.setVisibility(View.VISIBLE);

			vBrewery.setVisibility(View.GONE);
		} else if (before) {
			vContainer.setBackgroundResource(R.drawable.bg_card_bottom);
			vPaddingT.setVisibility(View.GONE);
			vPaddingB.setVisibility(View.VISIBLE);
			vSeparator.setVisibility(View.GONE);

			vBrewery.setVisibility(View.GONE);
		} else if (after) {
			vContainer.setBackgroundResource(R.drawable.bg_card_top);
			vPaddingT.setVisibility(View.VISIBLE);
			vPaddingB.setVisibility(View.GONE);
			vSeparator.setVisibility(View.VISIBLE);

			vBrewery.setVisibility(View.VISIBLE);
		} else {
			vContainer.setBackgroundResource(R.drawable.bg_card_alone);
			vPaddingT.setVisibility(View.VISIBLE);
			vPaddingB.setVisibility(View.VISIBLE);
			vSeparator.setVisibility(View.GONE);

			vBrewery.setVisibility(View.VISIBLE);
		}

		vName.setText(beer.name);

		vTapSince.setText(mContext.getString(R.string.card_tapped, mDateFormat.format(new Date(beer.onTapSince))));
		if (beer.onTapPrevious > 0) {
			vTapPrev.setText(mDateFormat.format(new Date(beer.onTapPrevious)));
		} else {
			vTapPrev.setText(R.string.card_first_time);
		}

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
		} else if (position < 0 || position > mList.size() - 1) {
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
