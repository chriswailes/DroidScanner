package com.nuvsoft.android.scanner;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.listeners.PhoneStateLogger;
import com.nuvsoft.android.scanner.observers.SMSObserver;
import com.nuvsoft.android.scanner.receivers.BatteryReceiver;
import com.nuvsoft.android.scanner.receivers.WifiReceiver;
import com.nuvsoft.android.scanner.settings.EventTrigger;
import com.nuvsoft.android.scanner.tasks.ScannerTask;
import com.nuvsoft.android.scanner.tasks.SyncTask;

// Logs:
// * Location (via network or gps)
// * Incoming SMS (data or text)
// * Outgoing Calls
// * Incoming Calls
// * Cell-Service Connection Changes
// * Data-Service Connection Changes

// TODO:
// * Outgoing/Incoming SMS (data or text)
// * Wifi Actvity

public class ScannerService extends Service {
	private static final String LOG_TAG = ScannerService.class.getSimpleName();
	public static final int MAIN_POLL_INTERVAL = 10 * 60 * 1000;
	private PhoneStateListener phLogger = null;
	private TelephonyManager phManager = null;
	// private TrackerWifiReceiver wifiReceiver = null;
	// private WifiManager mainWifi = null;
	private Handler handlerin = new Handler();
	private static SMSObserver smsObserver;
	private PowerManager.WakeLock wl;
	private Timer timer;
	private BatteryReceiver batteryReceiver;
	private WifiReceiver wifiReceiver;

	private static List<ScannerTask> trackerTasks = null;
	private static ScannerService instance = null;

	public void onCreate() {
		super.onCreate();

		instance = this;

		Log.d("TrackerService", "Start Everything Running...");

		/**
		 * TODO: Register all broadcast receivers in the service and not in the
		 * manifest. That way when the service is destroyed they can be
		 * unregistered.
		 */

		// acquire a partial wake lock
		wl = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						ScannerService.class.getPackage().getName());
		wl.acquire();

		// setup the telephony listener
		phManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phLogger = new PhoneStateLogger(
				(ScannerApplication) getApplicationContext());
		phManager.listen(phLogger, PhoneStateListener.LISTEN_CALL_STATE
				| PhoneStateListener.LISTEN_DATA_ACTIVITY
				| PhoneStateListener.LISTEN_SERVICE_STATE
				| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
				| PhoneStateListener.LISTEN_SIGNAL_STRENGTH);

		// setup sms listener
		// NOTE: Use getApplicationContext and not 'this' as the context must be
		// of type TrackerApplication
		smsObserver = new SMSObserver(getApplicationContext(), handlerin);

		// setup wifi receiver
		wifiReceiver = new WifiReceiver(
				(ScannerApplication) getApplicationContext());
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.NETWORK_STATE_CHANGED_ACTION));
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.NETWORK_IDS_CHANGED_ACTION));
		this.registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// registerReceiver(wifiReceiver, receiverFilter);

		/**
		 * TODO: Figure out why you have to register this in the service and not
		 * via the Manifest.
		 */
		batteryReceiver = new BatteryReceiver(
				(ScannerApplication) getApplicationContext());
		this.registerReceiver(batteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		timer = new Timer();
	}

	public static SMSObserver getSmsObserver() {
		return smsObserver;
	}

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		this.setForeground(true);
		Log.v(LOG_TAG, "Timer Started");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Log.v(LOG_TAG, "POLLING EVENTS");
				runEvent(EventTrigger.POLLING_EVENT);
			}
		}, 0, 30000);
	}

	public void onDestroy() {
		Log.d(LOG_TAG, "Service Destroyed.");
		wl.release();
		this.unregisterReceiver(wifiReceiver);
		this.unregisterReceiver(batteryReceiver);
		super.onDestroy();
	}

	private void runEvent(final EventTrigger eventTrigger) {
		if (!SyncTask.isSyncing()) {
			Log.v(LOG_TAG, "Running Event");
			if (trackerTasks == null) {
				trackerTasks = DatabaseAssistant
						.readSettings(getApplicationContext());
			}
			int eventid = -1;
			for (final ScannerTask tt : trackerTasks) {
				// Thread t = new Thread(new Runnable() {
				// public void run() {
				// tt.run(getApplicationContext(), eventTrigger);
				// }
				// });
				// t.start();
				eventid = tt
						.run(getApplicationContext(), eventTrigger, eventid);
			}
		} else {
			Log.v(LOG_TAG, "Waiting for Sync Event to Complete");
		}
	}

	public IBinder onBind(Intent intent) {
		return new ScannerServiceInterface.Stub() {
			public void onEvent(String e) throws RemoteException {
				EventTrigger eventTrigger = null;
				for (EventTrigger et : EventTrigger.values()) {
					if (et.name().compareTo(e) == 0) {
						eventTrigger = et;
						break;
					}
				}

				if (eventTrigger != null) {
					runEvent(eventTrigger);
				}
			}
		};
	}

	public static ScannerService getInstance() {
		return instance;
	}
}
