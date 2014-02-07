package carnero.princ;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.Helper;
import carnero.princ.fragment.KulovyBleskFragment;
import carnero.princ.fragment.OchutnavkovaPivniceFragment;
import carnero.princ.fragment.PrincMiroslavFragment;
import carnero.princ.fragment.ZlyCasyFragment;
import carnero.princ.model.Beer;
import carnero.princ.model.BeerList;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class MainActivity extends Activity {

	private SharedPreferences mPreferences;
	private Helper mHelper;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private RelativeLayout mDrawerContainer;
	private ListView mDrawerList;
	private Switch mSettingsSwitch;
	private int mSelectedPub;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		mPreferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		mHelper = new Helper(this);

		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();

		mDrawerContainer = (RelativeLayout) findViewById(R.id.drawer);

		mDrawerList = (ListView) findViewById(R.id.drawer_list);
		mDrawerList.setAdapter(new DrawerItemAdapter());
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();

				EasyTracker tracker = EasyTracker.getInstance(MainActivity.this);
				tracker.send(MapBuilder.createEvent("navigation", "drawer_open", null, null).build());
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mSettingsSwitch = (Switch) findViewById(R.id.settings_switch);
		mSettingsSwitch.setChecked(mPreferences.getBoolean(Constants.PREF_NOTIFICATION, true));

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mSelectedPub = mPreferences.getInt(Constants.PREF_PUB, Constants.LIST_PRINC.id);
		selectItem(mSelectedPub);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();

		DownloadService.setAlarmIntent(getApplicationContext());
	}

	@Override
	protected void onStart() {
		super.onStart();

		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	protected void onPause() {
		mPreferences.edit()
				.putInt(Constants.PREF_PUB, mSelectedPub)
				.commit();

		super.onPause();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;

		getActionBar().setTitle(mTitle);
	}

	public void onSettingsSwitched(View view) {
		boolean enabled = mSettingsSwitch.isChecked();

		mPreferences.edit()
				.putBoolean(Constants.PREF_NOTIFICATION, enabled)
				.commit();
	}

	private void selectItem(int id) {
		mSelectedPub = id;

		BeerList beerList = Utils.getBeerListById(id);

		Fragment fragment;
		if (beerList.id == Constants.LIST_PRINC.id) {
			fragment = PrincMiroslavFragment.instantiate(this, PrincMiroslavFragment.class.getName());
		} else if (beerList.id == Constants.LIST_ZLY.id) {
			fragment = ZlyCasyFragment.instantiate(this, ZlyCasyFragment.class.getName());
		} else if (beerList.id == Constants.LIST_PIVNICE.id) {
			fragment = OchutnavkovaPivniceFragment.instantiate(this, OchutnavkovaPivniceFragment.class.getName());
		} else if (beerList.id == Constants.LIST_KULOVY.id) {
			fragment = KulovyBleskFragment.instantiate(this, KulovyBleskFragment.class.getName());
		} else {
			return;
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();

		setTitle(getString(beerList.nameRes));
		mDrawerLayout.closeDrawer(mDrawerContainer);
	}

	// classess

	private class DrawerItemAdapter extends BaseAdapter {

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.item_drawer, parent, false);
			}

			BeerList beerList = getItem(position);
			ArrayList<Beer> beers = mHelper.loadBeers(beerList.id, true);

			((TextView) view.findViewById(R.id.title)).setText(beerList.nameRes);

			TextView good = (TextView) view.findViewById(R.id.good);
			if (beers.isEmpty()) {
				good.setVisibility(View.INVISIBLE);
				good.setText(null);
			} else {
				good.setText(String.valueOf(beers.size()));
				good.setVisibility(View.VISIBLE);
			}

			return view;
		}

		@Override
		public int getCount() {
			return Constants.LIST.size();
		}

		@Override
		public BeerList getItem(int position) {
			return Constants.LIST.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).id;
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			int beerListID = Constants.LIST.get(position).id;
			selectItem(beerListID);
		}
	}
}
