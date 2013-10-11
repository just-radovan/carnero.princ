package carnero.princ;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import carnero.princ.common.Constants;
import carnero.princ.database.Helper;
import carnero.princ.model.Beer;
import carnero.princ.view.RatingView;

public class RatingDialogFragment extends DialogFragment {

	protected Helper mHelper;
	protected Beer mBeer;
	protected TextView vBeerName;
	protected SeekBar vRating;
	protected RatingView vGraph;
	protected Button vConfirm;

	public static RatingDialogFragment newInstance(Fragment parent, long beerID) {
		Bundle arguments = new Bundle();
		arguments.putLong(Constants.EXTRA_BEER_ID, beerID);

		RatingDialogFragment fragment = new RatingDialogFragment();
		fragment.setTargetFragment(parent, 1001);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mHelper = new Helper(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View layout = inflater.inflate(R.layout.dialog_rating, container, false);

		vBeerName = (TextView) layout.findViewById(R.id.beer_name);
		vGraph = (RatingView) layout.findViewById(R.id.graph);
		vRating = (SeekBar) layout.findViewById(R.id.rating);
		vConfirm = (Button) layout.findViewById(R.id.confirm);

		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();

		Beer beer = getBeer();
		float rating = mHelper.getRating(beer.id);

		vBeerName.setText(beer.name);
		vRating.setProgress((int) rating);
		vGraph.setRating(rating);
		vRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Beer beer = getBeer();
				beer.rating = progress;
				vGraph.setRating(beer.rating);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// empty
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// empty
			}
		});

		vConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Beer beer = getBeer();
				mHelper.updateBeerRating(beer.id, beer.rating);

				if (getTargetFragment() instanceof MainFragment) {
					((MainFragment) getTargetFragment()).updateRating(beer.id, beer.rating);
				}

				dismiss();
			}
		});
	}

	protected Beer getBeer() {
		long id = getArguments().getLong(Constants.EXTRA_BEER_ID);
		if (mBeer == null || mBeer.id != id) {
			mBeer = mHelper.loadBeer(id);
		}

		return mBeer;
	}
}
