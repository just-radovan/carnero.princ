package carnero.princ.common;

import carnero.princ.model.Beer;

import java.util.Comparator;

public class BeerAZComparator implements Comparator<Beer> {

	public int compare(Beer b1, Beer b2) {
		if (b1.brewery == null) {
			return -1;
		} else if (b2.brewery == null) {
			return 1;
		}

		int breweryCompared = b1.brewery.compareToIgnoreCase(b2.brewery);
		if (breweryCompared != 0) {
			return breweryCompared;
		}

		if (b1.name == null) {
			return -1;
		} else if (b2.name == null) {
			return 1;
		}

		return b1.name.compareToIgnoreCase(b2.name);
	}
}
