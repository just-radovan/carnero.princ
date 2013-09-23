package carnero.princ;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.common.Utils;
import carnero.princ.database.ListLoader;
import carnero.princ.database.Structure;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.internet.ListDownloader;
import carnero.princ.model.Beer;
import carnero.princ.model.Hours;

import java.util.ArrayList;
import java.util.Calendar;

public class MainFragment extends Fragment implements ILoadingStatusListener {

	private ListView mList;
	private ImageView mProgress;
	private View mHeader;
	private BeerListAdapter mAdapter;

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
			headerStatus.setText("Do " + Utils.addLeadingZero(hours.fromHrs, 2) + ":" + Utils.addLeadingZero(hours.fromMns, 2) + " zavřeno");
		} else if (timeNow < timePubTo) {
			headerStatus.setText("Otevřeno do " + Utils.addLeadingZero(hours.toHrs, 2) + ":" + Utils.addLeadingZero(hours.toMns, 2));
		} else {
			headerStatus.setText("Již zavřeno");
		}

		if (mHeader != null) {
			mList.removeHeaderView(mHeader);
		}
		mList.addHeaderView(mHeader, null, false);

		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();

		new ListDownloader(getActivity(), null).execute(); // TODO: move to Service

		new ListLoader(getActivity(), this).execute(Structure.Table.PUB_PRINC);
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

		mProgress.clearAnimation();
		mProgress.setVisibility(View.GONE);
	}
}
