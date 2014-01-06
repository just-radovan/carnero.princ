package carnero.princ.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import carnero.princ.BeerListAdapter;
import carnero.princ.R;
import carnero.princ.common.BeerAZComparator;
import carnero.princ.common.BeerRatingComparator;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.database.ListLoader;
import carnero.princ.iface.IDownloadingStatusListener;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.internet.ListDownloader;
import carnero.princ.model.Beer;
import carnero.princ.model.BeerList;
import carnero.princ.model.Hours;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public abstract class AbstractFragment extends Fragment implements ILoadingStatusListener, IDownloadingStatusListener {

	protected BeerList mPub = Constants.LIST_PRINC;
	protected ArrayList<Beer> mBeers = new ArrayList<Beer>();
	protected BeerListAdapter mAdapter;
	protected SharedPreferences mPreferences;
	protected int mSort = Constants.SORT_ALPHABET;
	protected int mLastFirst = 0;
	//
	protected PullToRefreshListView vList;
	protected TextView vLastDownload;
	protected View vPanel;
	protected View vPanelContent;
	protected TextView vOpeningHours;
	protected ImageView vProgress;
	//
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

		vProgress = (ImageView) layout.findViewById(R.id.loading);
		vList = (PullToRefreshListView) layout.findViewById(R.id.beer_list);
		vPanel = layout.findViewById(R.id.panel);
		vPanelContent = layout.findViewById(R.id.panel_content);
		vOpeningHours = (TextView) layout.findViewById(R.id.status);
		vLastDownload = (TextView) layout.findViewById(R.id.update);

		setListView();
		setOpeningHours();

		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();

		Helper helper = new Helper(getActivity().getBaseContext());
		if (!helper.isSomeCurrentBeer()) { // no saved beer, download now
			new ListDownloader(getActivity().getBaseContext(), this).execute();
		} else {
			new ListLoader(getActivity().getBaseContext(), this).execute(mPub.id);
		}

		EasyTracker tracker = EasyTracker.getInstance(getActivity());
		tracker.send(MapBuilder.createEvent("navigation", "beer_list", "pub:" + mPub.id, null).build());
	}

	@Override
	public void onLoadingStart() {
		if (!isAdded()) {
			return;
		}

		AnimationDrawable animation = (AnimationDrawable) vProgress.getBackground();
		animation.start();

		vProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoadingComplete(ArrayList<Beer> list) {
		if (!isAdded() || list == null) {
			return;
		}

		mBeers.clear();
		mBeers.addAll(list);
		sortBeers();

		mAdapter.notifyDataSetChanged();
		vList.onRefreshComplete();

		setLastUpdate();

		vProgress.clearAnimation();
		vProgress.setVisibility(View.GONE);
	}

	@Override
	public void onDownloadingStart() {
		if (!isAdded()) {
			return;
		}

		AnimationDrawable animation = (AnimationDrawable) vProgress.getBackground();
		animation.start();

		vProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onDownloadingComplete() {
		new ListLoader(getActivity(), this).execute(mPub.id);
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

	protected void setListView() {
		mAdapter = new BeerListAdapter(getActivity());
		mAdapter.setData(mBeers);

		vList.setAdapter(mAdapter);
		vList.setOnItemClickListener(new BeerClickListener(this));
		vList.setShowViewWhileRefreshing(true);
		vList.setScrollEmptyView(false);
		vList.setShowIndicator(false);
		vList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new ListDownloader(getActivity(), AbstractFragment.this, mPub.id).execute(true);
			}
		});
		vList.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > 0 && mLastFirst == 0) { // hide
					vPanelContent.animate()
							.alpha(0.0f)
							.setDuration(300)
							.start();
					vPanel.animate()
							.translationYBy(+ getResources().getDimension(R.dimen.panel_height))
							.setDuration(500)
							.start();

					mLastFirst = firstVisibleItem;
				} else if (firstVisibleItem == 0 && mLastFirst != 0) { // show
					vPanel.animate()
							.translationY(0)
							.setDuration(500)
							.start();
					vPanelContent.animate()
							.alpha(1.0f)
							.setDuration(600)
							.start();

					mLastFirst = firstVisibleItem;
				}
			}
		});
	}

	protected void setOpeningHours() {
		Calendar calendar = Calendar.getInstance();
		Hours hours = mPub.hours[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		int timePubFrom = hours.fromHrs * 60 + hours.fromMns;
		int timePubTo = hours.toHrs * 60 + hours.toMns;
		if (timePubTo < timePubFrom) { // closing after midnight
			timePubTo += 24 * 60;
		}
		int timeNow = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

		if (timeNow < timePubFrom) {
			int hrs = hours.fromHrs;
			if (hrs == 24) {
				hrs = 0;
			}
			vOpeningHours.setText(getString(R.string.pub_closed, Utils.addLeadingZero(hrs, 2), Utils.addLeadingZero(hours.fromMns, 2)).toUpperCase());
		} else if (timeNow < timePubTo) {
			int hrs = hours.toHrs;
			if (hrs == 24) {
				hrs = 0;
			}
			vOpeningHours.setText(getString(R.string.pub_open, Utils.addLeadingZero(hrs, 2), Utils.addLeadingZero(hours.toMns, 2)).toUpperCase());
		} else {
			vOpeningHours.setText(getString(R.string.pub_tomorrow).toUpperCase());
		}
	}

	protected void setLastUpdate() {
		Calendar calendar = Calendar.getInstance();

		long last = mPreferences.getLong(mPub.prefLastDownload, 0);
		if (last <= 0) {
			vLastDownload.setText(R.string.no_last_download);
		} else {
			int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
			calendar.setTimeInMillis(last);
			int updatedDay = calendar.get(Calendar.DAY_OF_YEAR);

			if (currentDay == updatedDay) {
				vLastDownload.setText(sTimeFormat.format(calendar.getTime()));
			} else {
				vLastDownload.setText(sDateFormat.format(calendar.getTime()));
			}
		}
	}

	// classes

	public class BeerClickListener implements AdapterView.OnItemClickListener {

		private AbstractFragment mParent;

		public BeerClickListener(AbstractFragment parent) {
			mParent = parent;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			RatingDialogFragment fragment = RatingDialogFragment.newInstance(mParent, id);
			fragment.show(getFragmentManager(), "ratingDialog");
		}
	}
}
