package carnero.princ;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import carnero.princ.common.Constants;
import carnero.princ.database.Helper;
import carnero.princ.model.BeerList;
import carnero.princ.model.BestOfBeers;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockBeers extends DashClockExtension {

	protected void onUpdateData(int reason) {
		SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		int lastPub = preferences.getInt(Constants.PREF_PUB, -1);
		if (lastPub < 0 || lastPub >= Constants.LIST.size()) {
			return;
		}

		BeerList beerList = Constants.LIST.get(lastPub);

		Helper helper = new Helper(getApplicationContext());
		BestOfBeers bestOf = helper.loadGoodBeers(beerList.id);

		StringBuilder breweries = new StringBuilder();
		for (String brewery : bestOf.breweries) {
			if (breweries.length() > 0) {
				breweries.append(", ");
			}
			breweries.append(brewery);
		}

		Intent intent = new Intent(this, MainActivity.class);

		if (bestOf.count == 0) {
			publishUpdate(new ExtensionData()
					.visible(false)
			);
		} else {
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
}
