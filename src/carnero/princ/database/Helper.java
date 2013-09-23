package carnero.princ.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.model.Beer;

import java.util.ArrayList;

public class Helper extends SQLiteOpenHelper {

	public Helper(Context context) {
		super(context, Structure.name, null, Structure.version);
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

	public void saveBeers(ArrayList<Beer> list) {
		if (list == null || list.isEmpty()) {
			return;
		}

		SQLiteDatabase database = getWritableDatabase();

		StringBuilder where;
		ContentValues values;
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

			values = new ContentValues();
			values.put(Structure.Table.Beers.col_pub, beer.pub);
			values.put(Structure.Table.Beers.col_name, beer.name);
			values.put(Structure.Table.Beers.col_current, (beer.current ? 1 : 0));
			values.put(Structure.Table.Beers.col_rating, beer.rating);

			try {
				int cnt = database.update(Structure.Table.Beers.name, values, where.toString(), null);
				if (cnt > 0) {
					continue;
				}
			} catch (Exception e) {
				// pokemon
			}

			try {
				values.put(Structure.Table.Beers.col_tap_since, System.currentTimeMillis());

				long id = database.insert(Structure.Table.Beers.name, null, values);
				if (id > 0) {
					continue;
				}
			} catch (Exception e) {
				// pokemon
			}
		}

		database.close();
	}
}
