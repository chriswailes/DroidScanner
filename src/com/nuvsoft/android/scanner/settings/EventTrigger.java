package com.nuvsoft.android.scanner.settings;

public enum EventTrigger {
	/**
	 * Log information at a discrete polling interval.
	 */
	POLLING_EVENT(0),
	/**
	 * Log information when an outgoing call is detected.
	 */
	CALL_OUTGOING(1),
	/**
	 * Log information when an incoming call is detected.
	 */
	CALL_INCOMING(2),
	/**
	 * Log information when an outgoing sms is detected.
	 */
	SMS_SENT(3),
	/**
	 * Log information when an incoming sms is detected.
	 */
	SMS_RECEIVED(4),
	/**
	 * Log information when an sms is sent or received.
	 */
	SMS_SENT_OR_RECEIVED(5),
	/**
	 * Log information when the location is changed.
	 */
	LOCATION_CHANGED(6),
	/**
	 * Wifi network settings have been changed.
	 */
	WIFI_NETWORK_STATE_CHANGED(7),
	/**
	 * A scan has been performed.
	 */
	WIFI_SCAN_RESULTS_AVAILABLE(8), WIFI_SUPPLICANT_CONNECTION_CHANGED(9), WIFI_SUPPLICANT_STATE_CHANGED(
			10),
	/**
	 * Wifi was enabled or disabled.
	 */
	WIFI_STATE_CHANGED(11),
	/**
	 * Log information when the battery level changes.
	 */
	BATTERY_CHANGED(12);

	public static EventTrigger getEventByName(String name) {
		for (EventTrigger t : EventTrigger.values())
			if (t.name().equalsIgnoreCase(name))
				return t;
		return null;
	}

	private long value;

	private EventTrigger(int shiftValue) {
		value = 1 << shiftValue;
	}

	/**
	 * @return A settings value which will always be in the form 2^n.
	 */
	public long getEventTriggerMask() {
		return value;
	}

	/**
	 * Checks to see if a LogAction has been set.
	 * 
	 * @param value
	 *            - Settings value.
	 * @return true if setting has been set in the settings value.
	 */
	public boolean isSet(long settingsValue) {
		return (value & settingsValue) > 0;
	}
}
