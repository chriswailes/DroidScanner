package com.nuvsoft.android.scanner.settings;

public enum EventTrigger {
	/**
	 * Log information at a discrete polling interval.
	 */
	POLLING_EVENT,
	/**
	 * Log information when an outgoing call is detected.
	 */
	CALL_OUTGOING,
	/**
	 * Log information when an incoming call is detected.
	 */
	CALL_INCOMING,
	/**
	 * Log information when an outgoing sms is detected.
	 */
	SMS_SENT,
	/**
	 * Log information when an incoming sms is detected.
	 */
	SMS_RECEIVED,
	/**
	 * Log information when an sms is sent or received.
	 */
	SMS_SENT_OR_RECEIVED,
	/**
	 * Log information when the location is changed.
	 */
	LOCATION_CHANGED,
	/**
	 * Wifi network settings have been changed.
	 */
	WIFI_NETWORK_STATE_CHANGED,
	/**
	 * A scan has been performed.
	 */
	WIFI_SCAN_RESULTS_AVAILABLE, WIFI_SUPPLICANT_CONNECTION_CHANGED, WIFI_SUPPLICANT_STATE_CHANGED,
	/**
	 * Wifi was enabled or disabled.
	 */
	WIFI_STATE_CHANGED,
	/**
	 * Log information when the battery level changes.
	 */
	BATTERY_CHANGED;

	public static EventTrigger getEventByName(String name) {
		for (EventTrigger t : EventTrigger.values())
			if (t.name().equalsIgnoreCase(name))
				return t;
		return null;
	}
}
