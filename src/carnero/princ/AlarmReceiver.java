package carnero.princ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import carnero.princ.common.Constants;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10) { // do not download when pub is closed
			return;
		}

		Intent serviceIntent = new Intent(context, DownloadService.class);
		context.startService(serviceIntent);
	}
}
