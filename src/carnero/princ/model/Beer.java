package carnero.princ.model;

public class Beer {

	public long id;
	public int pub;
	public boolean current;
	public String brewery;
	public String name;
	public long onTapSince;
	public long onTapPrevious;
	public float rating;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Beer)) {
			return false;
		}

		if (name != null && brewery.equalsIgnoreCase(((Beer) o).brewery) && name.equalsIgnoreCase(((Beer) o).name)) {
			return true;
		} else {
			return false;
		}
	}
}
