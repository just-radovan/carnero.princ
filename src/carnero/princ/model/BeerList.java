package carnero.princ.model;

public class BeerList {

	public int id;
	public String url;
	public Hours[] hours;
	public String prefLastDownload;

	public BeerList(int id, String url, String prefLastDownload, Hours[] hours) {
		this.id = id;
		this.url = url;
		this.hours = hours;
		this.prefLastDownload = prefLastDownload;
	}
}
