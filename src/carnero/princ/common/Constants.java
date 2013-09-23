package carnero.princ.common;

import carnero.princ.model.Hours;

public class Constants {

	public static final String TAG = "princova piva";
	public static final String LIST_URL_PRINC = "http://uprincemiroslava.eu/nabidka-piv/";
	public static final String LIST_URL_ZLY = "http://zlycasy.eu/index.php?page=2";
	public static final String UNTAPPD_URL = "https://untappd.com/search?q=%s";

	public static final Hours[] HOURS = new Hours[] {
			new Hours(12, 00, 22, 30), // 0, sun
			new Hours(11, 00, 23, 00), // 1, mon
			new Hours(11, 00, 23, 00), // 2, tue
			new Hours(11, 00, 23, 00), // 3, wed
			new Hours(11, 00, 23, 00), // 4, thu
			new Hours(11, 00, 24, 00), // 5, fri
			new Hours(12, 00, 24, 00), // 6, sat
	};
}
