package carnero.princ;

import android.app.*;
import android.os.Bundle;
import carnero.princ.fragment.OchutnavkovaPivniceFragment;
import carnero.princ.fragment.PrincMiroslavFragment;
import carnero.princ.fragment.ZlyCasyFragment;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_main);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab()
				.setTabListener(new TabListener<PrincMiroslavFragment>(this, PrincMiroslavFragment.class))
				.setText(R.string.tab_princ)
		);
		actionBar.addTab(actionBar.newTab()
				.setTabListener(new TabListener<ZlyCasyFragment>(this, ZlyCasyFragment.class))
				.setText(R.string.tab_zly)
		);
		actionBar.addTab(actionBar.newTab()
				.setTabListener(new TabListener<OchutnavkovaPivniceFragment>(this, OchutnavkovaPivniceFragment.class))
				.setText(R.string.tab_pivnice)
		);
		actionBar.setSelectedNavigationItem(0);
	}

	@Override
	protected void onResume() {
		super.onResume();

		DownloadService.setAlarmIntent(getApplicationContext());
	}

	// classess

	private class TabListener<T extends Fragment> implements ActionBar.TabListener {

		protected Activity mActivity;
		protected Class<T> mClass;
		protected Fragment mFragment;

		public TabListener(Activity activity, Class<T> clz) {
			mActivity = activity;
			mClass = clz;
		}

		@Override
		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction) {
			mFragment = Fragment.instantiate(mActivity, mClass.getName());

			FragmentTransaction trans = getFragmentManager().beginTransaction()
					.replace(R.id.content, mFragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			trans.commit();
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction) {
			if (mFragment != null) {
				transaction.detach(mFragment);
			}
		}

		@Override
		public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction) {
			// empty
		}
	}
}
