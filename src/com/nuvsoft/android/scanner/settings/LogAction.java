package com.nuvsoft.android.scanner.settings;

public enum LogAction {
	/**
	 * Sync Event!
	 */
	SYNC_TASK,
	/**
	 * Log Wifi Scans
	 */
	LOG_WIFI,
	/**
	 * Log incoming sms message sizes.
	 */
	LOG_SMS_INCOMING,
	/**
	 * Log outgoing sms message sizes.
	 */
	LOG_SMS_OUTGOING,
	/**
	 * Log all sms message sizes.
	 */
	LOG_SMS_ALL,
	/**
	 * Log location from network location service.
	 */
	LOG_LOCATION_NETWORK,
	/**
	 * Log location from gps location service.
	 */
	LOG_LOCATION_GPS,
	/**
	 * Log most accurate location from gps or network location service.
	 */
	LOG_LOCATION_BEST,
	/**
	 * Log least accurate location from gps or network location service.
	 */
	LOG_LOCATION_WORST,
	/**
	 * Log call state of the phone.
	 */
	LOG_CALL_STATE,
	/**
	 * Log data transmission activity.
	 */
	LOG_DATA_STATE,
	/**
	 * Log phone service information.
	 */
	LOG_PHONE_SERVICE_STATE,
	/**
	 * Log battery level.
	 */
	LOG_BATTERY_LEVEL;

	public static LogAction getActionByName(String name) {
		for (LogAction a : LogAction.values())
			if (a.name().equalsIgnoreCase(name))
				return a;
		return null;
	}
}
