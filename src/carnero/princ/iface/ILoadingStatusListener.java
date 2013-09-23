package carnero.princ.iface;

import carnero.princ.model.Beer;

import java.util.ArrayList;

public interface ILoadingStatusListener {

	public void onLoadingStart();
	public void onLoadingComplete(ArrayList<Beer> list);
}
