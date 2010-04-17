package com.nuvsoft.android.scanner.tasks;

import android.content.Context;

public class CallStateLogTask extends ScannerTask {

	public CallStateLogTask(long t, long maxInterval) {
		super(t, maxInterval);
	}

	public String getLogTag() {
		return CallStateLogTask.class.getSimpleName();
	}

	@Override
	public boolean run(Context c, int eventid) {
		// TODO FILL OUT CALL STATE LOG TASK
		return false;
	}
}
