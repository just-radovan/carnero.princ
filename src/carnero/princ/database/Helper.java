package carnero.princ.database;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import carnero.princ.MainActivity;
import carnero.princ.R;
import carnero.princ.common.Constants;
import carnero.princ.model.Beer;
import carnero.princ.model.BeerAZComparator;
import carnero.princ.model.BeerName;

import java.util.ArrayList;
import java.util.Collections;

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
			db.execSQL(Structure.getBeersIndex());
		} catch (SQLException e) {
			Log.e(Constants.TAG, "Failed to create database");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// empty
	}

	public void saveBeers(ArrayList<Beer> list, int pub) {
		if (list == null || list.isEmpty()) {
			return;
		}

		SQLiteDatabase database = getWritableDatabase();
		ArrayList<BeerName> currentBeers = loadCurrentBeersID(database, pub);
		ArrayList<Long> updatedIDs = new ArrayList<Long>();
		ArrayList<String> newBeers = new ArrayList<String>();

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
			where.append(Structure.Table.Beers.col_name);
			where.append(" = \"");
			where.append(beer.name);
			where.append("\"");

			// check if beer is already saved
			id = -1;
			try {
				cursor = database.query(Structure.Table.Beers.name, new String[] {Structure.Table.Beers.col_id}, where.toString(), null, null, null, null);
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
			values.put(Structure.Table.Beers.col_name, beer.name);
			values.put(Structure.Table.Beers.col_current, (beer.current ? 1 : 0));
			values.put(Structure.Table.Beers.col_rating, beer.rating);

			if (id >= 0) { // update beer
				try {
					boolean alreadyOnTap = false;
					for (BeerName current : currentBeers) {
						if (current.id == id) {
							alreadyOnTap = true;
						}
					}
					if (!alreadyOnTap) {
						values.put(Structure.Table.Beers.col_tap_since, System.currentTimeMillis());
						newBeers.add(beer.name);
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
					newBeers.add(beer.name);

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
		long last = mPreferences.getLong(Constants.PREF_LAST_DOWNLOAD, 0);
		for (BeerName current : currentBeers) {
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
			StringBuilder text = new StringBuilder();
			for (String newBeer : newBeers) {
				text.append("\n→ ");
				text.append(newBeer);
			}

			Intent intent = new Intent(mContext, MainActivity.class);
			PendingIntent pending = PendingIntent.getActivity(mContext, Constants.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification.BigTextStyle style = new Notification.BigTextStyle();
			style.setBigContentTitle(mContext.getText(R.string.app_name));
			style.bigText(text.toString());

			Notification.Builder builder = new Notification.Builder(mContext);
			builder.setContentTitle(mContext.getText(R.string.app_name));
			builder.setContentText(mContext.getText(R.string.notification_info));
			builder.setSmallIcon(R.drawable.ic_notification);
			builder.setContentInfo(String.valueOf(newBeers.size()));
			builder.setContentIntent(pending);
			builder.setAutoCancel(true);
			builder.setStyle(style);

			NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(Constants.NOTIFICATION_ID, builder.build());
		}
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

	public boolean isSomeCurrentBeer() {
		SQLiteDatabase database = getWritableDatabase();

		Cursor cursor = null;
		boolean any = false;
		try {
			cursor = database.query(
					Structure.Table.Beers.name,
					new String[] {Structure.Table.Beers.col_id},
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

	private ArrayList<BeerName> loadCurrentBeersID(SQLiteDatabase database, int pub) {
		ArrayList<BeerName> list = new ArrayList<BeerName>();

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
					new String[] {Structure.Table.Beers.col_id, Structure.Table.Beers.col_pub, Structure.Table.Beers.col_name},
					sql.toString(),
					null, null, null, null
			);

			if (cursor.moveToFirst()) {
				int idxID = cursor.getColumnIndex(Structure.Table.Beers.col_id);
				int idxPub = cursor.getColumnIndex(Structure.Table.Beers.col_pub);
				int idxName = cursor.getColumnIndex(Structure.Table.Beers.col_name);

				BeerName beer;
				do {
					beer = new BeerName();
					beer.id = cursor.getLong(idxID);
					beer.pub = cursor.getInt(idxPub);
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
}
