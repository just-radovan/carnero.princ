package carnero.princ.common;

import carnero.princ.model.BeerList;
import carnero.princ.model.Hours;

import java.util.ArrayList;

public class Constants {

	public static final String TAG = "princova piva";
	// preferences
	public static final String PREF_NAME = "beer";
	public static final String PREF_SORTING = "beer:sorting";
	// servers
	public static final String LIST_URL_BREWERIES = "http://i.carnero.cc/carnero.princ.json";
	public static final String UNTAPPD_URL = "https://untappd.com/search?q=%s";
	// extra
	public static final String EXTRA_BEER_ID = "carnero.princ.beer_ID";
	// sort
	public static final int SORT_ALPHABET = 1;
	public static final int SORT_RATING = 2;
	// stuff
	public static final String ALARM_ACTION = "carnero.princ.broadcast.Download";
	public static final int ALARM_DOWNLOAD = 47;
	public static final int NOTIFICATION_ID = 48;

	// beer lists
	public static ArrayList<BeerList> LIST;
	public static final BeerList LIST_PRINC = new BeerList(
			1,
			"http://uprincemiroslava.eu/nabidka-piv/",
			"utf-8",
			"beer:last_download:princ",
			new Hours[]{
					new Hours(12, 00, 22, 30), // 0, sun
					new Hours(11, 00, 23, 00), // 1, mon
					new Hours(11, 00, 23, 00), // 2, tue
					new Hours(11, 00, 23, 00), // 3, wed
					new Hours(11, 00, 23, 00), // 4, thu
					new Hours(11, 00, 24, 00), // 5, fri
					new Hours(12, 00, 24, 00) // 6, sat
			}
	);
	public static final BeerList LIST_ZLY = new BeerList(
			2,
			"http://zlycasy.eu/index.php?page=2",
			"cp1260",
			"beer:last_download:zly",
			new Hours[]{
					new Hours(17, 00, 01, 00), // 0, sun
					new Hours(11, 00, 02, 00), // 1, mon
					new Hours(11, 00, 02, 00), // 2, tue
					new Hours(11, 00, 02, 00), // 3, wed
					new Hours(11, 00, 02, 00), // 4, thu
					new Hours(11, 00, 02, 00), // 5, fri
					new Hours(17, 00, 02, 00) // 6, sat
			}
	);
	public static final BeerList LIST_PIVNICE = new BeerList(
			3,
			"http://www.ochutnavkovapivnice.cz/prave_na_cepu/",
			"utf-8",
			"beer:last_download:pivnice",
			new Hours[]{
					new Hours(16, 00, 24, 00), // 0, sun
					new Hours(15, 00, 24, 00), // 1, mon
					new Hours(15, 00, 24, 00), // 2, tue
					new Hours(15, 00, 24, 00), // 3, wed
					new Hours(15, 00, 24, 00), // 4, thu
					new Hours(15, 00, 24, 00), // 5, fri
					new Hours(16, 00, 24, 00) // 6, sat
			}
	);

	static {
		LIST = new ArrayList<BeerList>();
		LIST.add(LIST_PRINC);
		LIST.add(LIST_ZLY);
		LIST.add(LIST_PIVNICE);
	}
}
