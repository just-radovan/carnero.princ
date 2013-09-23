package carnero.princ.database;

public class Structure {

	public static final String name = "cc.beers";
	public static final int version = 1;

	public static class Table {

		public static class Beers {

			public static final String name = "beers";

			public static final String col_id = "_id"; // integer
			public static final String col_name = "name"; // text
			public static final String col_current = "current"; // integer, 0/1
			public static final String col_tap_since = "on_tap_since"; // integer
			public static final String col_tap_prev = "on_tap_prev"; // integer
			public static final String col_rating = "rating"; // float
		}
	}
}
