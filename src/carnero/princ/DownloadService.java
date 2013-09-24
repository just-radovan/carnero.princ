package carnero.princ;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.iface.ILoadingStatusListener;
import carnero.princ.internet.ListDownloader;
import carnero.princ.model.Beer;

import java.util.ArrayList;

public class DownloadService extends Service implements ILoadingStatusListener {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new ListDownloader(getBaseContext(), this).execute();

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLoadingStart() {
		Log.d(Constants.TAG, "Downloading beer list");
	}

	@Override
	public void onLoadingComplete(ArrayList<Beer> list) {
		setAlarmIntent(getApplicationContext());

		Log.d(Constants.TAG, "Download finished");
		stopSelf();
	}

	public static boolean setAlarmIntent(Context context) {
		if (isAlarmSet(context)) {
			return false;
		}

		PendingIntent pending = PendingIntent.getBroadcast(context, Constants.ALARM_DOWNLOAD, getIntent(context), PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		manager.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + (10 * 60 * 1000), // 10 mins
				AlarmManager.INTERVAL_HOUR * 4, // 4 hrs
				pending
		);

		return true;
	}

	public static boolean isAlarmSet(Context context) {
		return (PendingIntent.getBroadcast(context, Constants.ALARM_DOWNLOAD, getIntent(context), PendingIntent.FLAG_NO_CREATE) != null);
	}

	public static Intent getIntent(Context context) {
		Intent intent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
		intent.setAction(Constants.ALARM_ACTION);

		return intent;
	}
}
