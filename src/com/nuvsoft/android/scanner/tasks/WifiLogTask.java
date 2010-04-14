package com.nuvsoft.android.scanner.tasks;

import java.util.List;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.settings.EventTrigger;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiLogTask extends ScannerTask {
	private List<ScanResult> wifiList;
	private static WifiManager mainWifi = null;
	private static List<WifiConfiguration> configuredWifi;
	private static final String LOG_TAG = WifiLogTask.class.getSimpleName();
	private static volatile boolean polling = false;

	public WifiLogTask(EventTrigger t, long maxInterval) {
		super(t, maxInterval);
	}

	private void getMainWifi(Context c) {
		if (mainWifi == null) {
			mainWifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
			configuredWifi = mainWifi.getConfiguredNetworks();
		}
	}

	@Override
	public boolean run(Context c, int eventid) {
		boolean success = false;

		if (WifiLogTask.polling) {
			Log.d(LOG_TAG, "Wifi Log Task Polling In Progress.");
			return false;
		} else {
			WifiLogTask.polling = true;

			getMainWifi(c);

			wifiList = null;
			boolean wifiEnabled = mainWifi.isWifiEnabled();

			String startState = wifiEnabled ? "ON" : "OFF";
			Log.d(LOG_TAG, "WIFI STARTED " + startState);

			WifiManager.WifiLock wifiLock = mainWifi
					.createWifiLock(WifiLogTask.class.getName());

			// turn on the wifi
			while (!mainWifi.setWifiEnabled(true)) {
				Thread.yield();
			}

			// wait for it to come on. (not doing this can cause
			// the WifiLock to lock the wifi off and not on for the scan)
			while (!mainWifi.isWifiEnabled()) {
				Thread.yield();
			}

			if (mainWifi.isWifiEnabled()) {
				Log.d(LOG_TAG, "Wifi Is On");
				wifiLock.acquire();

				Log.d(LOG_TAG, "Logging Wifi Results...");

				do {
					/**
					 * Disconnect before doing a scan. If this code block is
					 * removed, android will try to connect to a familiar access
					 * point and scan results will come back empty.
					 */
					while (!mainWifi.disconnect()) {
						Log.d(LOG_TAG,
								"Waiting for Wifi to disconnect for scan.");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					/**
					 * Send a scan request directly. This runs asynchronously,
					 * so a sleep of 1 second has been inserted to allow the
					 * scan to complete.
					 */
					Log.d(LOG_TAG, "Sending Scan Request");
					mainWifi.startScan();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Log.d(LOG_TAG, "Retrieving Wifi List");
					wifiList = mainWifi.getScanResults();
				} while (wifiList == null);
				Log.d(LOG_TAG, "Wifi List Retreived.");

				if (wifiList.size() > 0) {
					for (ScanResult r : wifiList) {
						Log.d(LOG_TAG, "Logging Wifi Scan Result " + r.SSID);
						if (DatabaseAssistant.logWifiResult(c, eventid, r,
								knownWifi(r.SSID))) {
							Log.d(LOG_TAG, "Wifi Scan Successfully Logged.");
							success = true;
						} else {
							Log.d(LOG_TAG, "Wifi Scan LOG FAILURE.");
							success = false;
						}
					}
				}

				wifiLock.release();

				if (!wifiEnabled) {
					Log.d(LOG_TAG, "Shutting Down WIFI");
					while (!mainWifi.setWifiEnabled(false)) {
						Thread.yield();
					}
				} else {
					Log.d(LOG_TAG, "Reconnecting to access point.");
					mainWifi.reconnect(); // do not try to force a reconnect
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				String expectedState = wifiEnabled ? "ON" : "OFF";
				String currState = mainWifi.isWifiEnabled() ? "ON" : "OFF";
				Log.d(LOG_TAG, "WIFI SHOULD BE " + expectedState);
				Log.d(LOG_TAG, "WIFI IS " + currState);

			}
			WifiLogTask.polling = false;

			return success;
		}
	}

	private boolean knownWifi(String SSID) {
		for (WifiConfiguration w : configuredWifi) {
			if (w.SSID.compareTo(SSID) == 0)
				return true;
		}
		return false;
	}

	@Override
	public String getLogTag() {
		return WifiLogTask.class.getSimpleName();
	}
}
