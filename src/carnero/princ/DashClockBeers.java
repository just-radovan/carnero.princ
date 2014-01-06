package carnero.princ;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import carnero.princ.common.Constants;
import carnero.princ.database.Helper;
import carnero.princ.model.Beer;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import java.util.ArrayList;

public class DashClockBeers extends DashClockExtension {

	protected void onUpdateData(int reason) {
		SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		int lastPub = preferences.getInt(Constants.PREF_PUB, Constants.LIST_PRINC.id);

		Helper helper = new Helper(getApplicationContext());
		ArrayList<Beer> beers = helper.loadBeers(lastPub, true);
		ArrayList<String> breweries = new ArrayList<String>();

		for (Beer beer : beers) {
			if (!breweries.contains(beer.brewery)) {
				breweries.add(beer.brewery);
			}
		}

		Intent intent = new Intent(this, MainActivity.class);

		if (beers.isEmpty()) {
			publishUpdate(new ExtensionData()
					.visible(false)
			);
		} else {
			publishUpdate(new ExtensionData()
					.visible(true)
					.icon(R.drawable.ic_dashclock)
					.status(String.valueOf(beers.size()) + " | " + String.valueOf(breweries.size()))
					.expandedTitle(getResources().getQuantityString(R.plurals.dashclock_beers, beers.size(), beers.size()))
					.expandedBody(getResources().getQuantityString(R.plurals.dashclock_breweries, breweries.size(), breweries.size()))
					.clickIntent(intent)
			);
		}

		EasyTracker tracker = EasyTracker.getInstance(getApplicationContext());
		tracker.send(MapBuilder.createEvent("dashclock", "update", null, null).build());
	}
}
