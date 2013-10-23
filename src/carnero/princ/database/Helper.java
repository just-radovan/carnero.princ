package carnero.princ.database;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import carnero.princ.MainActivity;
import carnero.princ.R;
import carnero.princ.common.Constants;
import carnero.princ.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Helper extends SQLiteOpenHelper {

	private Context mContext;
	private SharedPreferences mPreferences;

	public Helper(Context context) {
		super(context, Structure.name, null, Structure.version);

		mContext = context;
		mPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(Structure.getBeersCreate());
			for (String index : Structure.getBeersIndex()) {
				db.execSQL(index);
			}
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Failed to create database");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table " + Structure.Table.Beers.name);

		onCreate(db);
	}

	public void saveBeers(ArrayList<Beer> list, BeerList beerList) {
		if (list == null || list.isEmpty()) {
			return;
		}

		SQLiteDatabase database = getWritableDatabase();
		ArrayList<BeerShort> currentBeers = loadCurrentBeersID(database, beerList.id);
		ArrayList<Long> updatedIDs = new ArrayList<Long>();
		HashMap<String, Integer> newBeers = new HashMap<String, Integer>();
		int newBeersTotal = 0;

		StringBuilder where;
		ContentValues values;
		Cursor cursor = null;
		long id;
		for (Beer beer : list) {
			where = new StringBuilder();
			where.append(Structure.Table.Beers.col_pub);
			where.append(" = ");
			where.append(beer.pub);
			where.append(" and ");
			where.append(Structure.Table.Beers.col_brewery);
			if (!TextUtils.isEmpty(beer.brewery)) {
				where.append(" = \"");
				where.append(beer.brewery);
				where.append("\"");
			} else {
				where.append(" is null");
			}
			where.append(" and ");
			where.append(Structure.Table.Beers.col_name);
			where.append(" = \"");
			where.append(beer.name);
			where.append("\"");

			// check if beer is already saved
			id = -1;
			try {
				cursor = database.query(Structure.Table.Beers.name, new String[]{Structure.Table.Beers.col_id}, where.toString(), null, null, null, null);
				if (cursor.moveToFirst()) {
					id = cursor.getLong(cursor.getColumnIndex(Structure.Table.Beers.col_id));
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

			// store new data
			values = new ContentValues();
			values.put(Structure.Table.Beers.col_pub, beer.pub);
			if (!TextUtils.isEmpty(beer.brewery)) {
				values.put(Structure.Table.Beers.col_brewery, beer.brewery);
			}
			values.put(Structure.Table.Beers.col_name, beer.name);
			values.put(Structure.Table.Beers.col_current, (beer.current ? 1 : 0));

			if (id >= 0) { // update beer
				try {
					boolean alreadyOnTap = false;
					for (BeerShort current : currentBeers) {
						if (current.id == id) {
							alreadyOnTap = true;
						}
					}
					if (!alreadyOnTap) {
						values.put(Structure.Table.Beers.col_tap_since, System.currentTimeMillis());
						if (newBeers.containsKey(beer.brewery)) {
							newBeers.put(beer.brewery, newBeers.get(beer.brewery) + 1);
						} else {
							newBeers.put(beer.brewery, 1);
						}
						newBeersTotal++;
					}

					int cnt = database.update(Structure.Table.Beers.name, values, Structure.Table.Beers.col_id + " = " + id, null);
					if (cnt > 0) {
						updatedIDs.add(id);
					}
				} catch (Exception e) {
					// pokemon
				}
			} else { // insert new beer
				try {
					values.put(Structure.Table.Beers.col_tap_since, System.currentTimeMillis());
					if (newBeers.containsKey(beer.brewery)) {
						newBeers.put(beer.brewery, newBeers.get(beer.brewery) + 1);
					} else {
						newBeers.put(beer.brewery, 1);
					}
					newBeersTotal++;

					id = database.insert(Structure.Table.Beers.name, null, values);
					if (id >= 0) {
						updatedIDs.add(id);
					}
				} catch (Exception e) {
					// pokemon
				}
			}
		}

		// update removed beers
		long last = mPreferences.getLong(beerList.prefLastDownload, 0);
		for (BeerShort current : currentBeers) {
			if (!updatedIDs.contains(current.id)) { // removed from tap
				values = new ContentValues();
				values.put(Structure.Table.Beers.col_current, 0);
				values.put(Structure.Table.Beers.col_tap_prev, last);

				database.update(Structure.Table.Beers.name, values, Structure.Table.Beers.col_id + " = " + current.id, null);
			}
		}

		database.close();

		// notify about new stuff
		if (!newBeers.isEmpty()) {
			Resources res = mContext.getResources();
			StringBuilder text = new StringBuilder();

			Set<String> newBreweries = newBeers.keySet();
			for (String newBrewery : newBreweries) {
				text.append("\n→ ");
				if (!TextUtils.isEmpty(newBrewery)) {
					text.append(newBrewery);
				} else {
					text.append(res.getText(R.string.card_brewery_unknown));
				}
				text.append(": ");
				text.append(res.getQuantityString(R.plurals.notification_beers, newBeers.get(newBrewery), newBeers.get(newBrewery)));
			}

			Intent intent = new Intent(mContext, MainActivity.class);
			PendingIntent pending = PendingIntent.getActivity(mContext, Constants.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
			style.setBigContentTitle(mContext.getText(R.string.app_name));
			style.bigText(text.toString());

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
			builder.setContentTitle(mContext.getText(R.string.app_name));
			builder.setContentText(mContext.getText(R.string.notification_info));
			builder.setSmallIcon(R.drawable.ic_notification);
			builder.setContentInfo(String.valueOf(newBeersTotal));
			builder.setContentIntent(pending);
			builder.setAutoCancel(true);
			builder.setStyle(style);

			NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(Constants.NOTIFICATION_ID, builder.build());
		}
	}

	public boolean saveBeer(Beer beer) {
		boolean status = false;

		if (beer == null) {
			return status;
		}

		SQLiteDatabase database = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Structure.Table.Beers.col_pub, beer.pub);
		values.put(Structure.Table.Beers.col_brewery, beer.brewery);
		values.put(Structure.Table.Beers.col_name, beer.name);
		values.put(Structure.Table.Beers.col_current, (beer.current ? 1 : 0));
		values.put(Structure.Table.Beers.col_rating, beer.rating);
		values.put(Structure.Table.Beers.col_tap_since, beer.onTapSince);
		values.put(Structure.Table.Beers.col_tap_prev, beer.onTapPrevious);

		try {
			long id = database.insert(Structure.Table.Beers.name, null, values);
			if (id >= 0) {
				status = true;
			}
		} finally {
			database.close();
		}

		return status;
	}

	public ArrayList<Beer> loadBeers(int pub, boolean current) {
		ArrayList<Beer> list = new ArrayList<Beer>();

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_pub);
		sql.append(" = ");
		sql.append(pub);
		if (current) {
			sql.append(" and ");
			sql.append(Structure.Table.Beers.col_current);
			sql.append(" = 1");
		}

		Cursor cursor = null;
		try {
			cursor = database.query(Structure.Table.Beers.name, Structure.Table.Beers.projection, sql.toString(), null, null, null, null);

			if (cursor.moveToFirst()) {
				int idxID = cursor.getColumnIndex(Structure.Table.Beers.col_id);
				int idxPub = cursor.getColumnIndex(Structure.Table.Beers.col_pub);
				int idxCurrent = cursor.getColumnIndex(Structure.Table.Beers.col_current);
				int idxBrewery = cursor.getColumnIndex(Structure.Table.Beers.col_brewery);
				int idxName = cursor.getColumnIndex(Structure.Table.Beers.col_name);
				int idxSince = cursor.getColumnIndex(Structure.Table.Beers.col_tap_since);
				int idxPrevious = cursor.getColumnIndex(Structure.Table.Beers.col_tap_prev);
				int idxRating = cursor.getColumnIndex(Structure.Table.Beers.col_rating);

				Beer beer;
				do {
					beer = new Beer();
					beer.id = cursor.getLong(idxID);
					beer.pub = cursor.getInt(idxPub);
					beer.current = (cursor.getInt(idxCurrent) == 1);
					beer.brewery = cursor.getString(idxBrewery);
					beer.name = cursor.getString(idxName);
					beer.onTapSince = cursor.getLong(idxSince);
					beer.onTapPrevious = cursor.getLong(idxPrevious);
					beer.rating = cursor.getFloat(idxRating);

					list.add(beer);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		Collections.sort(list, new BeerAZComparator());

		return list;
	}

	public BestOfBeers loadGoodBeers() {
		BestOfBeers bestOf = new BestOfBeers();

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_rating);
		sql.append(" > 70 and ");
		sql.append(Structure.Table.Beers.col_current);
		sql.append(" = 1");

		Cursor cursor = null;
		try {
			cursor = database.query(Structure.Table.Beers.name, Structure.Table.Beers.projection, sql.toString(), null, null, null, Structure.Table.Beers.col_rating + " desc");

			bestOf.count = cursor.getCount();
			if (cursor.moveToFirst()) {
				int idxPub = cursor.getColumnIndex(Structure.Table.Beers.col_pub);
				int idxBrewery = cursor.getColumnIndex(Structure.Table.Beers.col_brewery);

				int pub;
				String brewery;
				do {
					pub = cursor.getInt(idxPub);
					brewery = cursor.getString(idxBrewery);

					if (!bestOf.pubs.contains(pub)) {
						bestOf.pubs.add(pub);
					}
					if (!bestOf.breweries.contains(brewery)) {
						bestOf.breweries.add(brewery);
					}
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		return bestOf;
	}

	public boolean isSomeCurrentBeer() {
		SQLiteDatabase database = getWritableDatabase();

		Cursor cursor = null;
		boolean any = false;
		try {
			cursor = database.query(
					Structure.Table.Beers.name,
					new String[]{Structure.Table.Beers.col_id},
					Structure.Table.Beers.col_current + " = 1",
					null, null, null, null
			);

			if (cursor != null && cursor.getCount() > 0) {
				any = true;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return any;
	}

	private ArrayList<BeerShort> loadCurrentBeersID(SQLiteDatabase database, int pub) {
		ArrayList<BeerShort> list = new ArrayList<BeerShort>();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_pub);
		sql.append(" = ");
		sql.append(pub);
		sql.append(" and ");
		sql.append(Structure.Table.Beers.col_current);
		sql.append(" = 1");

		Cursor cursor = null;
		try {
			cursor = database.query(
					Structure.Table.Beers.name,
					new String[]{Structure.Table.Beers.col_id, Structure.Table.Beers.col_pub, Structure.Table.Beers.col_brewery, Structure.Table.Beers.col_name},
					sql.toString(),
					null, null, null, null
			);

			if (cursor.moveToFirst()) {
				int idxID = cursor.getColumnIndex(Structure.Table.Beers.col_id);
				int idxPub = cursor.getColumnIndex(Structure.Table.Beers.col_pub);
				int idxBrewery = cursor.getColumnIndex(Structure.Table.Beers.col_brewery);
				int idxName = cursor.getColumnIndex(Structure.Table.Beers.col_name);

				BeerShort beer;
				do {
					beer = new BeerShort();
					beer.id = cursor.getLong(idxID);
					beer.pub = cursor.getInt(idxPub);
					beer.brewery = cursor.getString(idxBrewery);
					beer.name = cursor.getString(idxName);

					list.add(beer);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	public Beer loadBeer(long id) {
		Beer beer = null;

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_id);
		sql.append(" = ");
		sql.append(id);

		Cursor cursor = null;
		try {
			cursor = database.query(Structure.Table.Beers.name, Structure.Table.Beers.projection, sql.toString(), null, null, null, null);

			if (cursor.moveToFirst()) {
				int idxID = cursor.getColumnIndex(Structure.Table.Beers.col_id);
				int idxPub = cursor.getColumnIndex(Structure.Table.Beers.col_pub);
				int idxCurrent = cursor.getColumnIndex(Structure.Table.Beers.col_current);
				int idxBrewery = cursor.getColumnIndex(Structure.Table.Beers.col_brewery);
				int idxName = cursor.getColumnIndex(Structure.Table.Beers.col_name);
				int idxSince = cursor.getColumnIndex(Structure.Table.Beers.col_tap_since);
				int idxPrevious = cursor.getColumnIndex(Structure.Table.Beers.col_tap_prev);
				int idxRating = cursor.getColumnIndex(Structure.Table.Beers.col_rating);

				beer = new Beer();
				beer.id = cursor.getLong(idxID);
				beer.pub = cursor.getInt(idxPub);
				beer.current = (cursor.getInt(idxCurrent) == 1);
				beer.brewery = cursor.getString(idxBrewery);
				beer.name = cursor.getString(idxName);
				beer.onTapSince = cursor.getLong(idxSince);
				beer.onTapPrevious = cursor.getLong(idxPrevious);
				beer.rating = cursor.getFloat(idxRating);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		return beer;
	}

	public ArrayList<BeerName> loadUnknownBeers() {
		ArrayList<BeerName> list = new ArrayList<BeerName>();

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_brewery);
		sql.append(" = \"\" or ");
		sql.append(Structure.Table.Beers.col_brewery);
		sql.append(" is null ");

		Cursor cursor = null;
		try {
			cursor = database.query(
					Structure.Table.Beers.name,
					new String[]{Structure.Table.Beers.col_id, Structure.Table.Beers.col_brewery, Structure.Table.Beers.col_name},
					sql.toString(),
					null, null, null, null
			);

			if (cursor.moveToFirst()) {
				int idxID = cursor.getColumnIndex(Structure.Table.Beers.col_id);
				int idxBrewery = cursor.getColumnIndex(Structure.Table.Beers.col_brewery);
				int idxName = cursor.getColumnIndex(Structure.Table.Beers.col_name);

				BeerName beer;
				do {
					beer = new BeerName();
					beer.id = cursor.getLong(idxID);
					beer.brewery = cursor.getString(idxBrewery);
					beer.name = cursor.getString(idxName);

					list.add(beer);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		return list;
	}

	public boolean updateBeerBrewery(BeerName beer) {
		boolean status = false;

		SQLiteDatabase database = getWritableDatabase();

		try {
			ContentValues values = new ContentValues();
			values.put(Structure.Table.Beers.col_brewery, beer.brewery);
			values.put(Structure.Table.Beers.col_name, beer.name);

			int cnt = database.update(Structure.Table.Beers.name, values, Structure.Table.Beers.col_id + " = " + beer.id, null);
			if (cnt > 0) {
				status = true;
			}
		} finally {
			database.close();
		}

		return status;
	}

	public float getRating(long id) {
		float rating = -1f;

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder sql = new StringBuilder();
		sql.append(Structure.Table.Beers.col_id);
		sql.append(" = ");
		sql.append(id);

		Cursor cursor = null;
		try {
			cursor = database.query(Structure.Table.Beers.name, new String[] {Structure.Table.Beers.col_rating}, sql.toString(), null, null, null, null);

			if (cursor.moveToFirst()) {
				rating = cursor.getFloat(cursor.getColumnIndex(Structure.Table.Beers.col_rating));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		return rating;
	}

	public boolean updateBeerRating(long id, float rating) {
		boolean status = false;

		SQLiteDatabase database = getWritableDatabase();

		try {
			ContentValues values = new ContentValues();
			values.put(Structure.Table.Beers.col_rating, rating);

			int cnt = database.update(Structure.Table.Beers.name, values, Structure.Table.Beers.col_id + " = " + id, null);
			if (cnt > 0) {
				status = true;
			}
		} finally {
			database.close();
		}

		return status;
	}
}
