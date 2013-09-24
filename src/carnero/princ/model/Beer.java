package carnero.princ.model;

public class Beer {

	public long id;
	public int pub;
	public boolean current;
	public String name;
	public long onTapSince;
	public long onTapPrevious;
	public float rating;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Beer)) {
			return false;
		}

		if (name != null && name.equalsIgnoreCase(((Beer) o).name)) {
			return true;
		} else {
			return false;
		}
	}
}
