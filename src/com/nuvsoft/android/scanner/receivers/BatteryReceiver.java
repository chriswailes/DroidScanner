package com.nuvsoft.android.scanner.receivers;

import com.nuvsoft.android.scanner.ScannerApplication;
import com.nuvsoft.android.scanner.settings.EventTrigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = BatteryReceiver.class.getSimpleName();
	private volatile static Intent intent = null;
	private static boolean gotInfo = false;
	private ScannerApplication ta;

	public BatteryReceiver(ScannerApplication ta) {
		this.ta = ta;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Send event to service
		if (intent != null) {
			BatteryReceiver.intent = intent;

			gotInfo = true;

			// if (context.getApplicationContext() instanceof
			// TrackerApplication) {
			ta.runEvent(EventTrigger.BATTERY_CHANGED.name());
			Log.d(LOG_TAG, "BATTERY_CHANGED EVENT SENT TO SERVICE!");
			// } else {
			// Log.d(LOG_TAG, "Context was not of type TrackerApplication");
			// }
		}
	}

	public static boolean gotInfo() {
		return gotInfo;
	}

	public static Intent getIntent() {
		return intent;
	}
}
