package com.nuvsoft.android.scanner.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public enum DatabaseTable {
	/**
	 * TODO: Create Phone State schemas. 3 sections - Call State, Service State,
	 * and Data State
	 */

	/**
	 * TODO: Create Bluetooth information table.
	 */

	/**
	 * TODO: Create accelerometer information table.
	 */

	/**
	 * TODO: Create data information table/sensor.
	 */

	/**
	 * TODO: Create service state information table.
	 */

	/**
	 * Global settings table.
	 */
	GLOBAL_SETTINGS_TABLE("( tag TEXT PRIMARY KEY, value TEXT )", true),
	/**
	 * Settings information table. Contains the action, event trigger, max
	 * polling interval, and any special arguments (such as location change
	 * threshold or accelerometer threshold).
	 */
	POLLING_SETTINGS_TABLE(
			"( id INTEGER PRIMARY KEY AUTOINCREMENT, action int(8), event int(8), period_max int, special_args TEXT)",
			true),
	/**
	 * Triggered event information table.
	 */
	TRACKER_EVENT_TABLE(
			"( id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp int(8), event text )",
			false),
	/**
	 * Location information table.
	 */
	LOCATION_DATA_TABLE(
			"( eventid int(8), lat REAL, long REAL, alt REAL, provider TEXT, accuracy REAL)",
			false),
	/**
	 * Wifi information table.
	 */
	WIFI_DATA_TABLE(
			"( eventid int(8), SSID TEXT, BSSID TEXT, capabilities TEXT, frequency int, level int, known_ap TEXT)",
			false),
	/**
	 * SMS information table.
	 */
	SMS_DATA_TABLE("( eventid int(8), size int, sms_context text )", false),

	/**
	 * Battery information table.
	 */
	BATTERY_DATA_TABLE(
			"( eventid int(8), level int, scale int, voltage int, temperature int,plugged text, status text, present text,technology text )",
			false);

	private String createString;
	private boolean persistant;

	private DatabaseTable(String createString, boolean persistent) {
		this.createString = String.format("CREATE TABLE %s %s", name(),
				createString);
		this.persistant = persistent;
	}

	/**
	 * This function runs a select all query on the database table (WHERE 1=0)
	 * given and returns false if an exception occurs.
	 * 
	 * @param tbl_name
	 * @return True if table exists, false otherwise.
	 */
	private boolean doesTblExist(SQLiteDatabase db) {
		Cursor rs = null;
		try {
			rs = db.rawQuery("SELECT * FROM " + name() + " WHERE 1=0", null);
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

	public boolean createTable(SQLiteDatabase db) {
		if (!doesTblExist(db)) {
			db.execSQL(createString);
			return doesTblExist(db);
		} else {
			return true;
		}
	}

	public void resetTable(SQLiteDatabase db) {
		if (!persistant) {
			if (doesTblExist(db)) {
				db.execSQL("DROP TABLE " + name());
				createTable(db);
			}
		}
	}
}
