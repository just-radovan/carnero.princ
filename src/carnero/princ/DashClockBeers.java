package carnero.princ;

import android.content.Intent;
import carnero.princ.database.Helper;
import carnero.princ.model.BestOfBeers;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockBeers extends DashClockExtension {

	protected void onUpdateData(int reason) {
		Helper helper = new Helper(getApplicationContext());
		BestOfBeers bestOf = helper.loadGoodBeers();

		StringBuilder breweries = new StringBuilder();
		for (String brewery : bestOf.breweries) {
			if (breweries.length() > 0) {
				breweries.append(", ");
			}
			breweries.append(brewery);
		}

		Intent intent = new Intent(this, MainActivity.class);

		publishUpdate(new ExtensionData()
				.visible(true)
				.icon(R.drawable.ic_dashclock)
				.status(getResources().getQuantityString(R.plurals.good_beers, bestOf.count, bestOf.count))
				.expandedTitle(getResources().getQuantityString(R.plurals.good_beers, bestOf.count, bestOf.count))
				.expandedBody(breweries.toString())
				.clickIntent(intent)
		);
	}
}
