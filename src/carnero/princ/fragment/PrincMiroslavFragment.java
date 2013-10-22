package carnero.princ.fragment;

import android.os.Bundle;
import carnero.princ.common.Constants;

public class PrincMiroslavFragment extends AbstractFragment {

	@Override
	public void onCreate(Bundle state) {
		mPub = Constants.PUB_PRINC;

		super.onCreate(state);
	}
}