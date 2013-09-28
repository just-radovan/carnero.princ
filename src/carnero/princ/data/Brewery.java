package carnero.princ.data;

public class Brewery {

	public String identifier;
	public String name;
	public boolean removeID;

	public Brewery(String identifier, String name, boolean removeID) {
		this.identifier = identifier;
		this.name = name;
		this.removeID = removeID;
	}
}
