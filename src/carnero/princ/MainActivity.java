package carnero.princ;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import carnero.princ.common.Constants;
import carnero.princ.database.Helper;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();

		DownloadService.setAlarmIntent(getApplicationContext());
	}
}
