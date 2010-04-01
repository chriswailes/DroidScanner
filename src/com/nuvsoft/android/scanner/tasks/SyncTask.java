package com.nuvsoft.android.scanner.tasks;

import java.io.File;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.db.HttpAssistant;
import com.nuvsoft.android.scanner.db.SyncAssistant;
import com.nuvsoft.android.scanner.settings.EventTrigger;

import android.content.Context;
import android.util.Log;

public class SyncTask extends ScannerTask {

	String NAME_OF_FILE;
	String NAME_OF_UPLOAD_FILE;
	String DEV_ID;
	static String LOG_TAG = SyncTask.class.getSimpleName();
	private static boolean first = true;
	private static volatile boolean isSyncing = false;

	public SyncTask(EventTrigger t, long maxInterval) {
		super(t, maxInterval);
	}

	@Override
	public String getLogTag() {
		return SyncTask.class.getSimpleName();
	}

	@Override
	public boolean run(Context c, int eventid) {
		if(first){
			//Do not sync the very first time.  Collect data first.
			first = false;
			return true;
		}
		Log.v(LOG_TAG, "STARTING SYNC");
		isSyncing = true;
		Log.v(LOG_TAG, "STARTED SYNC");
		boolean ret = uploadDB(c);
		Log.v(LOG_TAG, "ENDING SYNC");
		isSyncing = false;
		return ret;
	}

	private boolean uploadDB(Context c) {
		try {
			Log.v(LOG_TAG, "Exporting db to sd card.");
			NAME_OF_FILE = DatabaseAssistant.exportToDB(c);
			NAME_OF_UPLOAD_FILE = SyncAssistant.getFileName(c);
			if (NAME_OF_FILE != null) {
				Log.v(LOG_TAG, "DATABASE EXPORTED TO " + NAME_OF_FILE);
				Log.v(LOG_TAG, "Starting File Upload.");
				// FileInputStream fis = new FileInputStream(NAME_OF_FILE);
				File f = new File(NAME_OF_FILE);
				if (HttpAssistant.post(c,f)) {
					Log.v(LOG_TAG, "Finished File Upload.");
					DatabaseAssistant.resetDB(c);
					//delete the file
					while (!f.delete())
						Log.v(LOG_TAG, "ATTEMPTING DELETE!");
					Log.v(LOG_TAG, "File deleted.");
					return true;
				} else {
					//delete the file
					while (!f.delete())
						Log.v(LOG_TAG, "ATTEMPTING DELETE!");
					Log.v(LOG_TAG, "File deleted.");
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			c.deleteFile(NAME_OF_FILE);
			Log.v(LOG_TAG, "File deleted.");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isSyncing() {
		// if (isSyncing)
		// Log.v(LOG_TAG, "Service Syncing.");
		// else
		// Log.v(LOG_TAG, "Service Not Syncing.");
		return isSyncing;
	}
}
