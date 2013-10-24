package carnero.princ.common;

import java.util.Comparator;

public class StringLengthComparator implements Comparator<String> {

	public int compare(String b1, String b2) {
		if (b1.length() > b2.length()) {
			return -1;
		} else if (b1.length() < b2.length()) {
			return +1;
		} else {
			return 0;
		}
	}
}
