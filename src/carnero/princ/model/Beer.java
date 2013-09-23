package carnero.princ.model;

public class Beer {

	public long id;
	public boolean current;
	public String name;
	public long onTapSince; // update when current moves from 'false' to 'true'
	public long onTapPrevious; // update when current moves from 'true' to 'false'
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
