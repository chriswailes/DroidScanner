package com.nuvsoft.android.scanner.tasks;

import android.content.Context;
import android.util.Log;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.receivers.BatteryReceiver;

public class BatteryLogTask extends ScannerTask {
	private static final String LOG_TAG = BatteryLogTask.class.getSimpleName();

	public BatteryLogTask(long t, long maxInterval) {
		super(t, maxInterval);
	}

	@Override
	public boolean run(Context c, int eventid) {
		if (!BatteryReceiver.gotInfo()) {
			Log.d(LOG_TAG, "No Info Yet Received From Battery Receiver");
			return false;
		}
		if (DatabaseAssistant.logBatteryResult(c, BatteryReceiver.getIntent(),
				eventid)) {
			Log.d(LOG_TAG, "Battery Info Logged Successfully");
			return true;
		} else {
			Log.d(LOG_TAG, "Battery Info Logging Failed");
			return false;
		}
	}

	@Override
	public String getLogTag() {
		return BatteryLogTask.class.getSimpleName();
	}
}
