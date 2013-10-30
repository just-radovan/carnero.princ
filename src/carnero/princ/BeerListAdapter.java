package carnero.princ;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.model.Beer;
import carnero.princ.view.RatingView;

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

		Resources res = mContext.getResources();

		View vContainer = view.findViewById(R.id.container);
		View vPaddingT = view.findViewById(R.id.padding_top);
		View vPaddingB = view.findViewById(R.id.padding_bottom);
		View vSeparator = view.findViewById(R.id.top_end);
		TextView vBrewery = (TextView) view.findViewById(R.id.brewery_name);
		TextView vName = (TextView) view.findViewById(R.id.beer_name);
		RatingView vRating = (RatingView) view.findViewById(R.id.beer_rating);
		TextView vTapPrev = (TextView) view.findViewById(R.id.beer_tap_prev);
		TextView vTapSince = (TextView) view.findViewById(R.id.beer_tap_since);

		Beer beerPrev = getItem(position - 1);
		Beer beerNext = getItem(position + 1);
		boolean before = (beerPrev != null && ((beerPrev.brewery == null && beer.brewery == null) || (beerPrev.brewery != null && beerPrev.brewery.equalsIgnoreCase(beer.brewery))));
		boolean after = (beerNext != null && ((beerNext.brewery == null && beer.brewery == null) || (beerNext.brewery != null && beerNext.brewery.equalsIgnoreCase(beer.brewery))));

		if (!TextUtils.isEmpty(beer.brewery)) {
			vBrewery.setTextColor(res.getColor(R.color.text_orange));
			vBrewery.setText(beer.brewery);
		} else {
			vBrewery.setTextColor(res.getColor(R.color.text_gray));
			vBrewery.setText(R.string.card_brewery_unknown);
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
			vSeparator.setVisibility(View.VISIBLE);

			vBrewery.setVisibility(View.GONE);
		} else if (after) {
			vContainer.setBackgroundResource(R.drawable.bg_card_top);
			vPaddingT.setVisibility(View.VISIBLE);
			vPaddingB.setVisibility(View.GONE);
			vSeparator.setVisibility(View.GONE);

			vBrewery.setVisibility(View.VISIBLE);
		} else {
			vContainer.setBackgroundResource(R.drawable.bg_card_alone);
			vPaddingT.setVisibility(View.VISIBLE);
			vPaddingB.setVisibility(View.VISIBLE);
			vSeparator.setVisibility(View.GONE);

			vBrewery.setVisibility(View.VISIBLE);
		}

		vName.setText(beer.name);
		vRating.setRating(beer.rating);

		int days = Math.round((System.currentTimeMillis() - beer.onTapSince) / (24 * 60 * 60 * 1000));
		if (days <= 0) { // today
			days = 1;
		}

		vTapSince.setText(mContext.getResources().getQuantityString(R.plurals.card_tapped, days, days));
		if (beer.onTapPrevious > 0) {
			vTapPrev.setText(mDateFormat.format(new Date(beer.onTapPrevious)));
			vTapPrev.setVisibility(View.VISIBLE);
		} else {
			vTapPrev.setVisibility(View.GONE);
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

	public Beer findByID(long id) {
		if (mList == null) {
			return null;
		}

		for (Beer beer : mList) {
			if (beer.id == id) {
				return beer;
			}
		}

		return null;
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
