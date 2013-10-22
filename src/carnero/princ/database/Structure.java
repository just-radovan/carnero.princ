package carnero.princ.database;

public class Structure {

	public static final String name = "cc.beers";
	public static final int version = 2;

	public static class Table {

		public static class Beers {

			public static final String name = "beers";

			public static final String col_id = "_id"; // integer
			public static final String col_pub = "pub"; // integer
			public static final String col_brewery = "brewery"; // text
			public static final String col_name = "name"; // text
			public static final String col_current = "current"; // integer, 0/1
			public static final String col_tap_since = "on_tap_since"; // integer
			public static final String col_tap_prev = "on_tap_prev"; // integer
			public static final String col_rating = "rating"; // float

			public static final String[] projection = new String[] {
					col_id, col_pub, col_brewery, col_name, col_current, col_tap_since, col_tap_prev, col_rating
			};
		}
	}

	public static String getBeersCreate() {
		StringBuilder sql = new StringBuilder();
		sql.append("create table ");
		sql.append(Table.Beers.name);
		sql.append(" (");
		sql.append(Table.Beers.col_id);
		sql.append(" integer primary key autoincrement,");
		sql.append(Table.Beers.col_pub);
		sql.append(" integer not null,");
		sql.append(Table.Beers.col_brewery);
		sql.append(" text,");
		sql.append(Table.Beers.col_name);
		sql.append(" text not null,");
		sql.append(Table.Beers.col_current);
		sql.append(" integer default 1,");
		sql.append(Table.Beers.col_tap_since);
		sql.append(" integer,");
		sql.append(Table.Beers.col_tap_prev);
		sql.append(" integer,");
		sql.append(Table.Beers.col_rating);
		sql.append(" float default 0");
		sql.append(")");

		return sql.toString();
	}

	public static String[] getBeersIndex() {
		return new String[] {
				"create index if not exists idx_pub_current on " + Table.Beers.name + " (" + Table.Beers.col_pub + ", " + Table.Beers.col_current + ")",
				"create index if not exists idx_beer_name on " + Table.Beers.name + " (" + Table.Beers.col_brewery + ", " + Table.Beers.col_name + ")"
		};
	}
}
