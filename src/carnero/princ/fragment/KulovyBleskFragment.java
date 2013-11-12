package carnero.princ.fragment;

import android.os.Bundle;
import carnero.princ.common.Constants;

public class KulovyBleskFragment extends AbstractFragment {

	@Override
	public void onCreate(Bundle state) {
		mPub = Constants.LIST_KULOVY;

		super.onCreate(state);
	}
}