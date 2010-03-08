package com.nuvsoft.android.scanner.receivers;

import com.nuvsoft.android.scanner.ScannerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceBootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v(ServiceBootReceiver.class.getSimpleName(), intent
				.getAction());
		context.startService(new Intent(context, ScannerService.class));
	}
}
