package com.nuvsoft.android.scanner.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.BatteryManager;
import android.util.Log;

import com.nuvsoft.android.scanner.settings.EventTrigger;
import com.nuvsoft.android.scanner.settings.LogAction;
import com.nuvsoft.android.scanner.tasks.BatteryLogTask;
import com.nuvsoft.android.scanner.tasks.SMSLogTask;
import com.nuvsoft.android.scanner.tasks.ScannerTask;
import com.nuvsoft.android.scanner.tasks.SyncTask;
import com.nuvsoft.android.scanner.tasks.WifiLogTask;

/**
 * @author David Cheeseman
 * 
 *         Logging functions for the tracker database. Includes sensor data as
 *         well as information about settings.
 */
public class DatabaseAssistant {
	private static SQLiteDatabase db;
	private static final String db_name = "Tracker.db";
	private static final String LOG_TAG = DatabaseAssistant.class
			.getSimpleName();

	/**
	 * Event ordering table.
	 */
	private static final String db_event_tbl_name = "tracker_event";
	private static final String db_create_event_table = "CREATE TABLE "
			+ db_event_tbl_name
			+ " ( id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp int(8), event text )";

	/**
	 * Location information table.
	 */
	private static final String db_loc_data_tbl_name = "tracker_loc_data";
	private static final String db_create_loc_table = "CREATE TABLE "
			+ db_loc_data_tbl_name
			+ " ( eventid int(8), lat REAL, long REAL, alt REAL, provider TEXT, accuracy REAL)";

	/**
	 * Wifi information table.
	 */
	private static final String db_wifi_data_tbl_name = "tracker_wifi_data";
	private static final String db_create_wifi_table = "CREATE TABLE "
			+ db_wifi_data_tbl_name
			+ " ( eventid int(8), SSID TEXT, BSSID TEXT, capabilities TEXT, frequency int, level int, known_ap TEXT)";

	/**
	 * TODO: Create Bluetooth information table.
	 */

	/**
	 * TODO: Create accelerometer information table.
	 */

	/**
	 * SMS information table.
	 */
	private static final String db_sms_tbl_name = "tracker_call_state";
	private static final String db_create_sms_table = "CREATE TABLE "
			+ db_sms_tbl_name
			+ " ( eventid int(8), size int, sms_context text )";

	/**
	 * TODO: Create Phone State schemas. 3 sections - Call State, Service State,
	 * and Data State
	 */
	private static final String db_call_state_tbl_name = "tracker_call_state";
	private static final String db_create_call_state_tbl = "CREATE TABLE "
			+ db_call_state_tbl_name + " ()";

	private static final String db_service_state_tbl_name = "tracker_service_state";
	private static final String db_create_servicce_state_tbl = "CREATE TABLE "
			+ db_service_state_tbl_name + " ()";

	private static final String db_data_state_tbl_name = "tracker_data_state";
	private static final String db_create_data_state_tbl = "CREATE TABLE "
			+ db_data_state_tbl_name + " ()";

	/**
	 * Settings information table. Contains the action, event trigger, max
	 * polling interval, and any special arguments (such as location change
	 * threshold or accelerometer threshold).
	 */
	private static final String db_settings_tbl_name = "tracker_settings";
	private static final String db_create_settings = "CREATE TABLE "
			+ db_settings_tbl_name
			+ " ( id INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT, event TEXT, period_max int, special_args TEXT)";

	private static final String db_sync_tbl_name = "tracker_sync_info";
	private static final String db_create_sync = "CREATE TABLE "
			+ db_sync_tbl_name
			+ " (id INTEGER PRIMARY KEY, url text, passphrase text)";

	private static final String db_battery_tbl_name = "tracker_battery";
	private static final String db_create_battery = "CREATE TABLE "
			+ db_battery_tbl_name
			+ " ( eventid int(8), level int, scale int, voltage int, temperature int,plugged text, status text, present text,technology text )";

	public static String getDatabasePath(Context c) {
		getDB(c);
		return db.getPath();
	}

	public static String exportToDB(Context c) {
		SyncAssistant sa = new SyncAssistant(c, db);
		String ret = sa.exportData();
		Log.v(LOG_TAG, ret);
		return ret;
	}

	/**
	 * Called prior to any action used by TrackerDatabaseHelper. Ensures the
	 * database is properly loaded.
	 * 
	 * @param context
	 */
	private static void getDB(Context context) {
		if (db == null) {
			db = context.openOrCreateDatabase(db_name,
					SQLiteDatabase.CREATE_IF_NECESSARY, null);
			db.setLocale(Locale.getDefault());
			db.setLockingEnabled(true);
			db.setVersion(1);
			if (!doesTblExist(db_event_tbl_name))
				db.execSQL(db_create_event_table);
			if (!doesTblExist(db_loc_data_tbl_name))
				db.execSQL(db_create_loc_table);
			if (!doesTblExist(db_settings_tbl_name))
				db.execSQL(db_create_settings);
			if (!doesTblExist(db_wifi_data_tbl_name))
				db.execSQL(db_create_wifi_table);
			if (!doesTblExist(db_battery_tbl_name))
				db.execSQL(db_create_battery);
			if (!doesTblExist(db_sms_tbl_name))
				db.execSQL(db_create_sms_table);
			if (!doesTblExist(db_sync_tbl_name))
				db.execSQL(db_create_sync);
		}
	}

	/**
	 * This function runs a select all query on the database table (WHERE 1=0)
	 * given and returns false if an exception occurs.
	 * 
	 * @param tbl_name
	 * @return True if table exists, false otherwise.
	 */
	private static boolean doesTblExist(String tbl_name) {
		Cursor rs = null;
		try {
			rs = db.rawQuery("SELECT * FROM " + tbl_name + " WHERE 1=0", null);
			Log.v("CONTEXT", "TABLE EXISTS");
			return true;
		} catch (Exception ex) {
			Log.v("CONTEXT", "TABLE DOESN'T EXISTS");
			return false;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public static boolean logWifiResult(Context context, int eventid,
			ScanResult r, boolean knownWifi) {
		getDB(context);
		ContentValues values = new ContentValues();
		values.put("SSID", r.SSID);
		values.put("BSSID", r.BSSID);
		values.put("capabilities", r.capabilities);
		values.put("frequency", r.frequency);
		values.put("level", r.level);
		values.put("eventid", eventid);
		if (knownWifi)
			values.put("known_ap", "t");
		else
			values.put("known_ap", "f");
		return db.insert(db_wifi_data_tbl_name, null, values) != -1;
	}

	public static boolean setSyncSettings(Context context, String url,
			String pass) {
		getDB(context);
		ContentValues values = new ContentValues();
		values.put("id", 1);
		values.put("url", url);
		values.put("passphrase", pass);
		String[] whereargs = { "1" };
		if (db.update(db_sync_tbl_name, values, "id=?", whereargs) != 1)
			return db.insert(db_sync_tbl_name, null, values) != -1;
		else
			return true;
	}

	public static String getSyncPass(Context context) {
		getDB(context);
		Cursor c = db.query(db_sync_tbl_name, null, "id=?",
				new String[] { "1" }, null, null, null);
		if (c.moveToFirst()) {
			return c.getString(c.getColumnIndex("passphrase"));
		}
		return null;
	}

	public static String getSyncURL(Context context) {
		getDB(context);
		Cursor c = db.query(db_sync_tbl_name, null, "id=?",
				new String[] { "1" }, null, null, null);
		if (c.moveToFirst()) {
			return c.getString(c.getColumnIndex("url"));
		}
		return null;
	}

	/**
	 * @param cursor
	 * @return String representation of the cursor as a dash-separated value.
	 */
	public static String cursorToString(Cursor c) {
		String ret = "";
		for (int i = 0; i < c.getColumnCount(); i++) {
			ret += c.getColumnName(i) + "-" + c.getString(i) + " ";
		}
		return ret;
	}

	/**
	 * @param context
	 * @return
	 */
	public static List<ScannerTask> getSettings(Context context) {
		getDB(context);
		List<ScannerTask> settings = new LinkedList<ScannerTask>();
		// Cursor c = db.query(db_settings_tbl_name, null, null, null, null,
		// null,
		// null);
		// if (false && c.moveToFirst()) {
		// do {
		// String action = c.getString(c.getColumnIndex("action"));
		// String event = c.getString(c.getColumnIndex("event"));
		//
		// EventTrigger eventTrig = null;
		// LogAction logAction = null;
		//
		// for (EventTrigger e : EventTrigger.values()) {
		// if (e.name().compareTo(event.toUpperCase()) == 0) {
		// // good value
		// eventTrig = e;
		// break;
		// }
		// }
		//
		// for (LogAction l : LogAction.values()) {
		// if (l.name().compareTo(action) == 0) {
		// // good action
		// logAction = l;
		// break;
		// }
		// }
		//
		// if (logAction != null && eventTrig != null) {
		// /**
		// * TODO: Properly implement this so that it reads the
		// * settings from the server url and constructs proper task
		// * objects.
		// */
		// }
		//
		// } while (c.moveToNext());
		// c.close();
		// } else {
		if (true) {
			// settings are empty! read from server
			List<String> settingsReader = HttpAssistant
					.getSettingsFromServer(context);
			if (settingsReader == null || settingsReader.size() < 2)
				return null;
			int version = Integer.parseInt(settingsReader.remove(0));
			for (String setting : settingsReader) {
				// eg: "LOG_WIFI,CALL_OUTGOING,0"
				Log.v(LOG_TAG, setting);
				Log.v(LOG_TAG, "SETTINGS COUNT: " + settings.size());
				String[] split = setting.split(",");
				if (split.length >= 3) {
					LogAction action = LogAction.getActionByName(split[0]);
					EventTrigger trigger = EventTrigger
							.getEventByName(split[1]);
					long delay = Long.parseLong(split[2]);
					String extraArgs = "";
					if (split.length == 4)
						extraArgs = split[3];

					switch (action) {
					case SYNC_TASK:
						// no special cases;
						settings.add(new SyncTask(trigger, delay));
						break;
					case LOG_BATTERY_LEVEL:
						// no special cases;,
						settings.add(new BatteryLogTask(trigger, delay));
						break;
					case LOG_WIFI:
						// no special cases;
						settings.add(new WifiLogTask(trigger, delay));
						break;
					case LOG_SMS_INCOMING:
						switch (trigger) {
						case SMS_RECEIVED:
							settings
									.add(new SMSLogTask(action, trigger, delay));
							break;
						default:
							// do nothing, all other triggers are illegal
							break;
						}
						break;
					case LOG_SMS_OUTGOING:
						switch (trigger) {
						case SMS_SENT:
							settings
									.add(new SMSLogTask(action, trigger, delay));
							break;
						default:
							// do nothing, all other triggers are illegal
							break;
						}
						break;
					case LOG_SMS_ALL:
						switch (trigger) {
						case SMS_SENT_OR_RECEIVED:
							settings
									.add(new SMSLogTask(action, trigger, delay));
							break;
						default:
							// do nothing, all other triggers are illegal
							break;
						}
						break;
					}
				}
			}
		}
		// TODO: Get rid of this hacked in system.
		// settings.add(new WifiLogTask(EventTrigger.POLLING_EVENT, 30000));
		// settings
		// .add(new BatteryLogTask(EventTrigger.BATTERY_CHANGED, 10 * 1000));
		// settings.add(new WifiLogTask(EventTrigger.CALL_OUTGOING, 0));
		// settings
		// .add(new WifiLogTask(EventTrigger.POLLING_EVENT, 5 * 60 * 1000));
		// settings.add(new SyncTask(EventTrigger.POLLING_EVENT, 60 * 60 *
		// 1000));
		// settings.add(new WifiLogTask(EventTrigger.SMS_SENT, 0));
		if (settings.size() > 0) {
			return settings;
		} else {
			return null;
		}
	}

	public static boolean logLocationResult(Context context, Location location,
			int eventid) {
		getDB(context);
		ContentValues values = new ContentValues();
		values.put("lat", location.getLatitude());
		values.put("long", location.getLongitude());
		values.put("alt", location.getAltitude());
		values.put("accuracy", location.getAccuracy());
		values.put("provider", location.getProvider());
		values.put("eventid", eventid);
		return db.insert(db_loc_data_tbl_name, null, values) != -1;
	}

	public static boolean logSMSResult(Context context, int size,
			String sms_context, int eventid) {
		getDB(context);
		ContentValues values = new ContentValues();
		values.put("size", size);
		values.put("sms_context", sms_context);
		values.put("eventid", eventid);
		return db.insert(db_sms_tbl_name, null, values) != -1;
	}

	public static boolean logBatteryResult(Context context, Intent batIntent,
			int eventid) {
		if (batIntent == null)
			return false;
		getDB(context);
		// String[] batteryExtraKeys = { "level", "scale", "voltage",
		// "temperature", "plugged", "status", "health", "present",
		// "technology", "icon-small" };
		ContentValues values = new ContentValues();
		values.put("level", batIntent.getIntExtra("level", -1));
		values.put("scale", batIntent.getIntExtra("scale", -1));
		values.put("voltage", batIntent.getIntExtra("voltage", -1));
		values.put("temperature", batIntent.getIntExtra("temperature", -1));
		values.put("technology", batIntent.getExtras().getString("technology"));

		int plugged = batIntent.getIntExtra("plugged", -1);
		switch (plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			values.put("plugged", "AC_PLUGGED");
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			values.put("plugged", "USB_PLUGGED");
			break;
		default:
			values.put("plugged", "UNPLUGGED");
			break;
		}

		int status = batIntent.getIntExtra("status", -1);
		switch (status) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			values.put("status", "STATUS_CHARGING");
			break;
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			values.put("status", "STATUS_DISCHARGING");
			break;
		case BatteryManager.BATTERY_STATUS_FULL:
			values.put("status", "STATUS_FULL");
			break;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			values.put("status", "STATUS_NOT_CHARGING");
			break;
		case BatteryManager.BATTERY_STATUS_UNKNOWN:
			values.put("status", "STATUS_UNKNOWN");
			break;
		default:
			// this should not happen
			values.put("status", "STATUS_NOT_DOCUMENTED");
			break;
		}

		int health = batIntent.getIntExtra("health", -1);
		switch (health) {
		case BatteryManager.BATTERY_HEALTH_DEAD:
			values.put("status", "HEALTH_DEAD");
			break;
		case BatteryManager.BATTERY_HEALTH_GOOD:
			values.put("status", "HEALTH_GOOD");
			break;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			values.put("status", "HEALTH_OVER_VOLTAGE");
			break;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			values.put("status", "HEALTH_OVERHEAT");
			break;
		case BatteryManager.BATTERY_HEALTH_UNKNOWN:
			values.put("status", "HEALTH_UNKNOWN");
			break;
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			values.put("status", "HEALTH_UNSPECIFIED_FAILURE");
			break;
		default:
			// this should not happen
			values.put("status", "HEALTH_NOT_DOCUMENTED");
			break;
		}

		if (batIntent.getExtras().getBoolean("present")) {
			values.put("present", "BATTERY_PRESENT");
		} else {
			values.put("present", "BATTERY_NOT_PRESENT");
		}

		values.put("eventid", eventid);
		return db.insert(db_battery_tbl_name, null, values) != -1;
	}

	public static void purgeDB(Context context) {
		db.close();
		db = null;
		while (context.deleteDatabase(db_name))
			Log.v(LOG_TAG, "ATTEMPTING TO DELETE DATABASE!");
		getDB(context);
	}

	public static void resetDB(Context context) {
		String url = DatabaseAssistant.getSyncURL(context);
		String pass = DatabaseAssistant.getSyncPass(context);
		db.close();
		db = null;
		while (context.deleteDatabase(db_name))
			Log.v(LOG_TAG, "ATTEMPTING TO DELETE DATABASE!");
		getDB(context);
		DatabaseAssistant.setSyncSettings(context, url, pass);
	}

	public static int registerEvent(Context context, EventTrigger et) {
		getDB(context);
		ContentValues values = new ContentValues();
		values.put("event", et.name());
		values.put("timestamp", System.currentTimeMillis());
		if (db.insert(db_event_tbl_name, null, values) != -1) {
			String[] columns = { "id" };
			Cursor c = db.query(db_event_tbl_name, columns, null, null, null,
					null, "id DESC");
			if (c.moveToFirst()) {
				int ret = c.getInt(c.getColumnIndex("id"));
				c.close();
				return ret;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
