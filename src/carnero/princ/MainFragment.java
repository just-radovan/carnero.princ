package carnero.princ;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.database.ListLoader;
import carnero.princ.database.Structure;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.internet.ListDownloader;
import carnero.princ.model.Beer;
import carnero.princ.model.Hours;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainFragment extends Fragment implements ILoadingStatusListener {

	private ListView mList;
	private ImageView mProgress;
	private View mHeader;
	private View mFooter;
	private BeerListAdapter mAdapter;
	private static DateFormat sTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private static DateFormat sDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		View layout = inflater.inflate(R.layout.fragment_main, container, false);

		mList = (ListView) layout.findViewById(android.R.id.list);
		mProgress = (ImageView) layout.findViewById(R.id.loading);

		// opening hours
		Calendar calendar = Calendar.getInstance();
		Hours hours = Constants.HOURS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		int timePubFrom = hours.fromHrs * 60 + hours.fromMns;
		int timePubTo = hours.toHrs * 60 + hours.toMns;
		int timeNow = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

		mHeader = inflater.inflate(R.layout.item_opening_hours, null, false);
		TextView headerStatus = (TextView) mHeader.findViewById(R.id.status);

		if (timeNow < timePubFrom) {
			int hrs = hours.fromHrs;
			if (hrs == 24) {
				hrs = 0;
			}
			headerStatus.setText(getString(R.string.pub_closed, Utils.addLeadingZero(hrs, 2), Utils.addLeadingZero(hours.fromMns, 2)));
		} else if (timeNow < timePubTo) {
			int hrs = hours.toHrs;
			if (hrs == 24) {
				hrs = 0;
			}
			headerStatus.setText(getString(R.string.pub_open, Utils.addLeadingZero(hrs, 2), Utils.addLeadingZero(hours.toMns, 2)));
		} else {
			headerStatus.setText(R.string.pub_tomorrow);
		}

		if (mHeader != null) {
			mList.removeHeaderView(mHeader);
		}
		mList.addHeaderView(mHeader, null, false);

		mFooter = inflater.inflate(R.layout.item_last_update, null, false);
		if (mFooter != null) {
			mList.removeFooterView(mFooter);
		}
		mList.addFooterView(mFooter, null, false);

		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();

		Helper helper = new Helper(getActivity());
		if (!helper.isSomeCurrentBeer()) { // no saved beer, download now
			new ListDownloader(getActivity(), this).execute();
		} else {
			new ListLoader(getActivity(), this).execute(Structure.Table.PUB_PRINC);
		}
	}

	@Override
	public void onLoadingStart() {
		if (!isAdded()) {
			return;
		}

		AnimationDrawable animation = (AnimationDrawable) mProgress.getBackground();
		animation.start();

		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoadingComplete(ArrayList<Beer> list) {
		if (!isAdded() || list == null) {
			return;
		}

		mAdapter = new BeerListAdapter(getActivity());
		mAdapter.setData(list);

		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new BeerClickListener());

		mProgress.clearAnimation();
		mProgress.setVisibility(View.GONE);

		// last update
		SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		long last = preferences.getLong(Constants.PREF_LAST_DOWNLOAD, 0);
		Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.setTimeInMillis(last);
		int updatedDay = calendar.get(Calendar.DAY_OF_YEAR);

		if (currentDay == updatedDay) {
			((TextView) mFooter.findViewById(R.id.update)).setText(sTimeFormat.format(calendar.getTime()));
		} else {
			((TextView) mFooter.findViewById(R.id.update)).setText(sDateFormat.format(calendar.getTime()));
		}
	}

	public void updateRating(long id, float rating) {
		if (mAdapter != null) {
			Beer beer = mAdapter.findByID(id);
			if (beer != null) {
				beer.rating = rating;
			}

			mAdapter.notifyDataSetChanged();
		}
	}

	// classes

	public class BeerClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			RatingDialogFragment fragment = RatingDialogFragment.newInstance(MainFragment.this, id);
			fragment.show(getFragmentManager(), "ratingDialog");
		}
	}
}
