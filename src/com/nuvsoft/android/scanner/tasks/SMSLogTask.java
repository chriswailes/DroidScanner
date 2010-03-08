package com.nuvsoft.android.scanner.tasks;

import com.nuvsoft.android.scanner.ScannerService;
import com.nuvsoft.android.scanner.settings.EventTrigger;
import com.nuvsoft.android.scanner.settings.LogAction;

import android.content.Context;

public class SMSLogTask extends ScannerTask {

	private LogAction action;

	public SMSLogTask(LogAction a, EventTrigger t, long maxInterval) {
		super(t, maxInterval);
		this.action = a;
	}

	@Override
	public String getLogTag() {
		return SMSLogTask.class.getSimpleName();
	}

	@Override
	public boolean run(Context c, int eventid) {
		ScannerService.getSmsObserver().onEvent(action.name(),eventid);
		return false;
	}

}
