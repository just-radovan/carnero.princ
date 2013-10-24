package carnero.princ.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import carnero.princ.BeerListAdapter;
import carnero.princ.R;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.database.ListLoader;
import carnero.princ.iface.IDownloadingStatusListener;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.internet.ListDownloader;
import carnero.princ.model.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public abstract class AbstractFragment extends Fragment implements ILoadingStatusListener, IDownloadingStatusListener {

	protected BeerList mPub = Constants.LIST_PRINC;
	protected ArrayList<Beer> mBeers;
	protected ListView mList;
	protected ImageView mProgress;
	protected View mHeader;
	protected View mFooter;
	protected BeerListAdapter mAdapter;
	protected SharedPreferences mPreferences;
	protected int mSort = Constants.SORT_ALPHABET;
	protected static DateFormat sTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	protected static DateFormat sDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		mPreferences = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		mSort = mPreferences.getInt(Constants.PREF_SORTING, Constants.SORT_ALPHABET);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		View layout = inflater.inflate(R.layout.fragment_main, container, false);

		mList = (ListView) layout.findViewById(android.R.id.list);
		mProgress = (ImageView) layout.findViewById(R.id.loading);

		// opening hours
		Calendar calendar = Calendar.getInstance();
		Hours hours = mPub.hours[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		int timePubFrom = hours.fromHrs * 60 + hours.fromMns;
		int timePubTo = hours.toHrs * 60 + hours.toMns;
		if (timePubTo < timePubFrom) { // closing after midnight
			timePubTo += 24 * 60;
		}
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
			new ListLoader(getActivity(), this).execute(mPub.id);
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

		mBeers = list;
		sortBeers();

		mAdapter = new BeerListAdapter(getActivity());
		mAdapter.setData(mBeers);

		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new BeerClickListener());

		mProgress.clearAnimation();
		mProgress.setVisibility(View.GONE);

		// last update
		long last = mPreferences.getLong(mPub.prefLastDownload, 0);
		if (last <= 0) {
			((TextView) mFooter.findViewById(R.id.update)).setText(R.string.no_last_download);
		} else {
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
	}

	@Override
	public void onDownloadingStart() {
		if (!isAdded()) {
			return;
		}

		AnimationDrawable animation = (AnimationDrawable) mProgress.getBackground();
		animation.start();

		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onDownloadingComplete() {
		new ListLoader(getActivity(), this).execute(mPub.id);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		switch (mSort) {
			case Constants.SORT_ALPHABET:
				menu.findItem(R.id.change_sorting).setIcon(android.R.drawable.ic_menu_sort_by_size);
				break;
			case Constants.SORT_RATING:
				menu.findItem(R.id.change_sorting).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.change_sorting) {
			switch (mSort) {
				case Constants.SORT_ALPHABET:
					mSort = Constants.SORT_RATING;
					break;
				case Constants.SORT_RATING:
					mSort = Constants.SORT_ALPHABET;
					break;
			}

			mPreferences.edit()
					.putInt(Constants.PREF_SORTING, mSort)
					.commit();

			sortBeers();
			mAdapter.notifyDataSetChanged();

			return true;
		}

		return false;
	}

	protected void sortBeers() {
		getActivity().invalidateOptionsMenu();

		switch (mSort) {
			case Constants.SORT_ALPHABET:
				Collections.sort(mBeers, new BeerAZComparator());
				break;
			case Constants.SORT_RATING:
				Collections.sort(mBeers, new BeerRatingComparator());
				break;
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
			RatingDialogFragment fragment = RatingDialogFragment.newInstance(AbstractFragment.this, id);
			fragment.show(getFragmentManager(), "ratingDialog");
		}
	}
}
