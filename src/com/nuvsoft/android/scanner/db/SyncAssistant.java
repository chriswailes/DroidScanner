package com.nuvsoft.android.scanner.db;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;

/**
 * @author Michael Maitlen
 * 
 *         Taken from 
 *         http://mgmblog.com/2009/02/06/export-an-android-sqlite-db-to-an-
 *         xml-file-on-the-sd-card/ and slightly modified.
 */
public class SyncAssistant {
	//private static final String LOG_TAG = SyncAssistant.class.getSimpleName();
	private static final String EXPORT_FILE_NAME_BASE = "/sdcard/";
	private String EXPORT_FILE_NAME;

	private Context _ctx;
	private SQLiteDatabase _db;
	private Exporter _exporter;

	public SyncAssistant(Context ctx, SQLiteDatabase db) {
		_ctx = ctx;
		_db = db;

		try {
			// create a file on the sdcard to export the
			// database contents to

			EXPORT_FILE_NAME = EXPORT_FILE_NAME_BASE + getFileName(ctx);

			File myFile = new File(EXPORT_FILE_NAME);
			myFile.createNewFile();

			FileOutputStream fOut = new FileOutputStream(myFile);
			BufferedOutputStream bos = new BufferedOutputStream(fOut);

			_exporter = new Exporter(bos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ctx
	 * @return A string in the format [MD5_OF_HARDWARE_ID]_[TIME_SINCE_EPOC].xml
	 */
	public static String getFileName(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		return md5(tm.getDeviceId()) + "_"
				+ String.valueOf(System.currentTimeMillis()) + ".xml";
	}

	/**
	 * Taken from: http://www.androidsnippets.org/snippets/52/ Used to anonymize
	 * hardware ID.
	 * 
	 * @param s
	 * @return Md5 hash of string
	 */
	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String exportData() {
		try {
			_exporter.startDbExport(_db.getPath());

			// get the tables out of the given sqlite database
			String sql = "SELECT * FROM sqlite_master";

			Cursor cur = _db.rawQuery(sql, new String[0]);
			// Log.d("db", "show tables, cur size " + cur.getCount());
			cur.moveToFirst();

			String tableName;
			while (cur.getPosition() < cur.getCount()) {
				tableName = cur.getString(cur.getColumnIndex("name"));
				// log("table name " + tableName);

				// don't process these two tables since they are used
				// for metadata
				if (!tableName.equals("android_metadata")
						&& !tableName.equals("sqlite_sequence")) {
					exportTable(tableName);
				}

				cur.moveToNext();
			}
			_exporter.endDbExport();
			_exporter.close();
		} catch (Exception e) {
			//Log.v(LOG_TAG, "Returning Null For Some Reason");
			//Log.v(LOG_TAG, e.getMessage());
			return null;
		}
		//Log.v(LOG_TAG, "SUCCESSFUL EXPORT - " + EXPORT_FILE_NAME);
		return EXPORT_FILE_NAME;
	}

	private void exportTable(String tableName) throws Exception {
		if(tableName.equalsIgnoreCase(DatabaseTable.GLOBAL_SETTINGS_TABLE.getTableName())){
			return;
		}
		
		_exporter.startTable(tableName);

		// get everything from the table
		String sql = "select * from " + tableName;
		Cursor cur = _db.rawQuery(sql, new String[0]);
		int numcols = cur.getColumnCount();

		log("Start exporting table " + tableName);

		// // logging
		// for( int idx = 0; idx < numcols; idx++ )
		// {
		// log( "column " + cur.getColumnName(idx) );
		// }

		cur.moveToFirst();

		// move through the table, creating rows
		// and adding each column with name and value
		// to the row
		while (cur.getPosition() < cur.getCount()) {
			_exporter.startRow();
			String name;
			String val;
			for (int idx = 0; idx < numcols; idx++) {
				name = cur.getColumnName(idx);
				val = cur.getString(idx);
				log("col '" + name + "' -- val '" + val + "'");

				_exporter.addColumn(name, val);
			}

			_exporter.endRow();
			cur.moveToNext();
		}

		cur.close();

		_exporter.endTable();
	}

	private void log(String msg) {
		// Log.d("DatabaseAssistant", msg);
	}

	class Exporter {
		private static final String CLOSING_WITH_TICK = "'>";
		private static final String START_DB = "<export-database name='";
		private static final String END_DB = "</export-database>";
		private static final String START_TABLE = "<table name='";
		private static final String END_TABLE = "</table>";
		private static final String START_ROW = "<row>";
		private static final String END_ROW = "</row>";
		private static final String START_COL = "<col name='";
		private static final String END_COL = "</col>";

		private BufferedOutputStream _bos;

		public Exporter() throws FileNotFoundException {
			this(new BufferedOutputStream(_ctx.openFileOutput(EXPORT_FILE_NAME,
					Context.MODE_WORLD_READABLE)));
		}

		public Exporter(BufferedOutputStream bos) {
			_bos = bos;
		}

		public void close() throws Exception {
			if (_bos != null) {
				_bos.close();
			}
		}

		public void startDbExport(String dbName) throws Exception {
			String stg = START_DB + dbName + CLOSING_WITH_TICK;
			_bos.write(stg.getBytes());
		}

		public void endDbExport() throws Exception {
			_bos.write(END_DB.getBytes());
		}

		public void startTable(String tableName) throws Exception {
			String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
			_bos.write(stg.getBytes());
		}

		public void endTable() throws Exception {
			_bos.write(END_TABLE.getBytes());
		}

		public void startRow() throws Exception {
			_bos.write(START_ROW.getBytes());
		}

		public void endRow() throws Exception {
			_bos.write(END_ROW.getBytes());
		}

		public void addColumn(String name, String val) throws Exception {
			String stg = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
			_bos.write(stg.getBytes());
		}
	}

	class Importer {

	}
}