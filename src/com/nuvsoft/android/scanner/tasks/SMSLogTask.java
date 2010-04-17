package com.nuvsoft.android.scanner.tasks;

import android.content.Context;

import com.nuvsoft.android.scanner.ScannerService;

public class SMSLogTask extends ScannerTask {

	private long action;

	public SMSLogTask(long a, long t, long maxInterval) {
		super(t, maxInterval);
		this.action = a;
	}

	@Override
	public String getLogTag() {
		return SMSLogTask.class.getSimpleName();
	}

	@Override
	public boolean run(Context c, int eventid) {
		ScannerService.getSmsObserver().onEvent(action, eventid);
		return false;
	}
}
