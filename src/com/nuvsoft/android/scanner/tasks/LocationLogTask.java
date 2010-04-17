package com.nuvsoft.android.scanner.tasks;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;

public class LocationLogTask extends ScannerTask {
	private static final String LOG_TAG = LocationLogTask.class.getSimpleName();
	private static LocationManager locMgr = null;
	private static LocationListener locLstnr = new LocationListener() {
		public void onLocationChanged(Location arg0) {
		}

		public void onProviderDisabled(String arg0) {
		}

		public void onProviderEnabled(String arg0) {
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}

	};

	public LocationLogTask(long t, long maxInterval) {
		super(t, maxInterval);
	}

	@Override
	public boolean run(Context c, int eventid) {
		if (locMgr == null) {
			locMgr = (LocationManager) c
					.getSystemService(Context.LOCATION_SERVICE);
		}

		requestUpdates();

		// find most accurate location provider
		Location location = null;
		Location l1 = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location l2 = locMgr
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (!l1.hasAccuracy()) { // no gps fix, probably
			if (!l2.hasAccuracy()) {
			} // nothing works
			else
				location = l2;
		} else { // gps fixed, but is it any good?
			if (!l2.hasAccuracy())
				location = l1; // no network fix
			else {
				// pick the most accurate
				if (l2.getAccuracy() < l1.getAccuracy())
					location = l2;
				else
					location = l1;
			}
		}

		cleanup();

		if (DatabaseAssistant.logLocationResult(c, location, eventid)) {
			Log.v(LOG_TAG, "Succefully Logged Location");
			return true;
		} else {
			Log.v(LOG_TAG, "LOCATION LOG FAILED");
			return false;
		}
	}

	private void requestUpdates() {
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, // min
				// time
				// interval
				// in
				// ms
				// dev docs say this should
				// be higher than 60,000 to conserve
				// power...
				10.0f, // min distance interval in m
				locLstnr);
		locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, // min
				// time
				// interval
				// in
				// ms
				// dev docs say this should
				// be higher than 60,000 to conserve
				// power...
				10.0f, // min distance interval in m
				locLstnr);
	}

	public void cleanup() {
		locMgr.removeUpdates(locLstnr);
	}

	@Override
	public String getLogTag() {
		// TODO Auto-generated method stub
		return LocationLogTask.class.getSimpleName();
	}
}
