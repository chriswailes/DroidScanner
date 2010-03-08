package com.nuvsoft.android.scanner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.nuvsoft.android.scanner.ScannerApplication;
import com.nuvsoft.android.scanner.settings.EventTrigger;

public class WifiReceiver extends BroadcastReceiver {
	private ScannerApplication ta;

	public WifiReceiver(ScannerApplication ta) {
		this.ta = ta;
	}

	public void onReceive(Context context, Intent intent) {
		Log.d("WifiReceiver", "Received Intent: " + intent.getAction());
		if (intent.getAction().compareTo(
				WifiManager.NETWORK_STATE_CHANGED_ACTION) == 0) {
			ta.runEvent(EventTrigger.WIFI_NETWORK_STATE_CHANGED.name());
		} else if (intent.getAction().compareTo(
				WifiManager.NETWORK_IDS_CHANGED_ACTION) == 0) {
			// TODO: Consider if this is worth supporting...could be triggered
			// very often.
		} else if (intent.getAction().compareTo(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) == 0) {
			ta.runEvent(EventTrigger.WIFI_SCAN_RESULTS_AVAILABLE.name());
		} else if (intent.getAction().compareTo(
				WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) == 0) {
			ta.runEvent(EventTrigger.WIFI_SUPPLICANT_CONNECTION_CHANGED.name());
		} else if (intent.getAction().compareTo(
				WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) == 0) {
			ta.runEvent(EventTrigger.WIFI_SUPPLICANT_STATE_CHANGED.name());
		} else if (intent.getAction().compareTo(
				WifiManager.WIFI_STATE_CHANGED_ACTION) == 0) {
			ta.runEvent(EventTrigger.WIFI_STATE_CHANGED.name());
		}
	}
}