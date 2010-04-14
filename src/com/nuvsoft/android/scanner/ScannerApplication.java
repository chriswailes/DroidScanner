package com.nuvsoft.android.scanner;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;

public class ScannerApplication extends Application implements
		ServiceConnection {
	private static final String LOG_TAG = ScannerApplication.class
			.getSimpleName();

	private ScannerServiceInterface iface;

	public void onCreate() {
		super.onCreate();
		getInterface();
	}

	/**
	 * This function attempts to check for sync settings in the local database.
	 * Should the settings exist, it assumes they're good and starts the tracker
	 * service. A connection check is not done because the client may be trying
	 * to survey areas with low/no connectivity.
	 */
	public void startApplication() {
		if (DatabaseAssistant.getDatabasePath(this) != null
				&& DatabaseAssistant.getSyncURL(this) != null) {
			getInterface();
		}
	}

	public void stopApplication() {
		stopService();
	}

	private void stopService() {
		if (isServiceRunning(this)) {
			this.stopService(new Intent(this, ScannerService.class));
		} else {
			Log.v(LOG_TAG, "SERVICE ALREADY STOPPED");
		}

		iface = null;
	}

	private boolean getInterface() {
		if (!isServiceRunning(this)) {
			Log.v(LOG_TAG, startService(new Intent(this, ScannerService.class))
					.flattenToString());
		} else {
			Log.v(LOG_TAG, "SERVICE ALREADY STARTED");
		}
		bindService(new Intent(this, ScannerService.class), this,
				Context.BIND_AUTO_CREATE);
		return iface != null;
	}

	public void runEvent(String e) {
		if (getInterface()) { // inserted due to paranoia.
			try {
				iface.onEvent(e);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void onServiceConnected(ComponentName arg0, IBinder service) {
		if (iface == null)
			iface = ScannerServiceInterface.Stub.asInterface(service);
	}

	public void onServiceDisconnected(ComponentName arg0) {
		iface = null;
	}

	private boolean isServiceRunning(Context c) {
		ActivityManager am = ((ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningServiceInfo> processes = am
				.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : processes) {
			String pname = info.service.getShortClassName();
			Log.v(LOG_TAG, pname);
			if (pname.contains(ScannerService.class.getSimpleName()))
				return true;
		}
		Log.v(LOG_TAG, "Service Not Running");
		return false;
	}
}
