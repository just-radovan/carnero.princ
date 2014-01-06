package carnero.princ.model;

public class BeerList {

	public int id;
	public int nameRes;
	public String address;
	public String url;
	public String encoding;
	public Hours[] hours;
	public String prefLastDownload;

	public BeerList(int id, int nameRes, String address, String url, String encoding, String prefLastDownload, Hours[] hours) {
		this.id = id;
		this.nameRes = nameRes;
		this.address = address;
		this.url = url;
		this.encoding = encoding;
		this.hours = hours;
		this.prefLastDownload = prefLastDownload;
	}
}
