package carnero.princ.iface;

import carnero.princ.model.Beer;

import java.util.ArrayList;

public interface IDownloadStatusListener {

	public void onDownloadStarted();
	public void onDownloadCompleted(ArrayList<Beer> list);
}
