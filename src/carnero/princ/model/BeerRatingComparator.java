package carnero.princ.model;

import java.util.Comparator;

public class BeerRatingComparator implements Comparator<Beer> {

	public int compare(Beer b1, Beer b2) {
		if (b1.rating > b2.rating) {
			return -1;
		} else if (b1.rating < b2.rating) {
			return +1;
		} else {
			return 0;
		}
	}
}
