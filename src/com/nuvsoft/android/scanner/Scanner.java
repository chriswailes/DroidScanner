package com.nuvsoft.android.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.db.HttpAssistant;

/**
 * Trying to make a general swiss-army-knife data logger. So far, I've cribbed
 * source and ideas from the dev docs and Mark Murphy's
 * "Android Programming Tutorials".
 * 
 * @author Caleb Phillips <cphillips@smallwhitecube.com>, David Cheeseman
 *         <nuvious@gmail.com>
 * 
 */
public class Scanner extends Activity {

	final int MENU_QUIT = 1;
	final int EXIT_OKAY = 0;

	private static final String LOG_TAG = Scanner.class.getSimpleName();
	private Intent serviceIntent = null;

	private Button submit;
	private TextView tv_status;
	private EditText et_url, et_pass;
	private volatile boolean testing_connection = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d("Tracker", "Starting Tracker Service");
		// this starts the service IF it isn't already started, hence
		// if our UI is stopped and restarted, we won't rudely restart
		// the service, it just keeps on keepin' on in the &

		et_url = (EditText) Scanner.this.findViewById(R.id.et_url);
		et_pass = (EditText) Scanner.this.findViewById(R.id.et_pass);

		submit = (Button) this.findViewById(R.id.b_save_settings);
		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				DatabaseAssistant.setSyncSettings(getApplicationContext(),
						et_url.getText().toString(), et_pass.getText()
								.toString());
				updateSyncStatus();
			}
		});

		if (DatabaseAssistant.getSyncURL(this) != null
				&& DatabaseAssistant.getSyncURL(this) != null) {
			updateSyncStatus();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_QUIT, 0, "Quit");
		return true;
	}
	
	private void showProgressSpinner(){
		new Thread(new Runnable(){
			public void run() {
				Scanner.this.setProgressBarVisibility(true);
				int progress = 0;
				while(testing_connection){
					Scanner.this.setProgress(progress);
					progress++;
					progress %= 10000;
				}
				Scanner.this.setProgress(0);
				Scanner.this.setProgressBarVisibility(false);
			}
		}).start();
	}

	private void updateSyncStatus() {
		testing_connection = true;
		showProgressSpinner();
		tv_status = ((TextView) this.findViewById(R.id.tv_status));
		et_url = (EditText) Scanner.this.findViewById(R.id.et_url);
		et_pass = (EditText) Scanner.this.findViewById(R.id.et_pass);

		et_url.setText(DatabaseAssistant.getSyncURL(this));
		et_pass.setText(DatabaseAssistant.getSyncPass(this));

		tv_status.setText("Testing Settings...");
		if (HttpAssistant.testSettings(Scanner.this)) {
			tv_status
					.setText("Settings test successfull. Tracker Service Started.");
			((ScannerApplication) getApplicationContext()).startApplication();
		} else {
			tv_status.setText("Settings test failed.");
		}
		testing_connection = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_QUIT:
			// the only time we EVER stop the service is if the user
			// specifically exits the tracker program. If the tracker
			// is stopped by the OS for any reason, the service should
			// remain running.
			stopService(serviceIntent);
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v(LOG_TAG, "Shutting Down Tracker Activity");
		finish();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(LOG_TAG, "Stopping Tracker Activity");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(LOG_TAG, "Tracker Activity Destroyed");
	}
}