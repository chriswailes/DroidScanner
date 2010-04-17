package com.nuvsoft.android.scanner.settings;

import com.nuvsoft.android.scanner.tasks.BatteryLogTask;
import com.nuvsoft.android.scanner.tasks.LocationLogTask;
import com.nuvsoft.android.scanner.tasks.SMSLogTask;
import com.nuvsoft.android.scanner.tasks.ScannerTask;
import com.nuvsoft.android.scanner.tasks.SyncTask;
import com.nuvsoft.android.scanner.tasks.WifiLogTask;

public enum LogAction {
	/**
	 * Sync Event!
	 */
	SYNC_TASK(0) {
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new SyncTask(trigger, delay);
		}
	},
	/**
	 * Log Wifi Scans
	 */
	LOG_WIFI(1) {
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new WifiLogTask(trigger, delay);
		}
	},
	/**
	 * Log incoming sms message sizes.
	 */
	LOG_SMS_INCOMING(2){
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new SMSLogTask(getLogActionMask(), trigger, delay);
		}
	},
	/**
	 * Log outgoing sms message sizes.
	 */
	LOG_SMS_OUTGOING(3){
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new SMSLogTask(getLogActionMask(), trigger, delay);
		}
	},
	/**
	 * Log all sms message sizes.
	 */
	LOG_SMS_ALL(4) {
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new SMSLogTask(getLogActionMask(), trigger, delay);
		}
	},
	/**
	 * Log location from network location service.
	 */
	//LOG_LOCATION_NETWORK(5),
	/**
	 * Log location from gps location service.
	 */
	//LOG_LOCATION_GPS(6),
	/**
	 * Log most accurate location from gps or network location service.
	 */
	LOG_LOCATION_BEST(7) {
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new LocationLogTask(trigger,delay);
		}
	},
	/**
	 * Log least accurate location from gps or network location service.
	 */
	//LOG_LOCATION_WORST(8),
	/**
	 * Log call state of the phone.
	 */
	//LOG_CALL_STATE(9),
	/**
	 * Log data transmission activity.
	 */
	//LOG_DATA_STATE(10),
	/**
	 * Log phone service information.
	 */
	//LOG_PHONE_SERVICE_STATE(11),
	/**
	 * Log battery level.
	 */
	LOG_BATTERY_LEVEL(12) {
		@Override
		public ScannerTask generateTask(long trigger, long delay,
				String extraArgs) {
			return new BatteryLogTask(trigger, delay);
		}
	};

	private long value;

	public static LogAction getActionByName(String name) {
		for (LogAction a : LogAction.values())
			if (a.name().equalsIgnoreCase(name))
				return a;
		return null;
	}

	private LogAction(long shiftValue) {
		value = 1 << shiftValue;
	}

	/**
	 * @return A settings value which will always be in the form 2^n.
	 */
	public long getLogActionMask() {
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

	public abstract ScannerTask generateTask(long trigger, long delay,
			String extraArgs);
}
