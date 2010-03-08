package com.nuvsoft.android.scanner.listeners;

import com.nuvsoft.android.scanner.ScannerApplication;
import com.nuvsoft.android.scanner.settings.EventTrigger;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class PhoneStateLogger extends PhoneStateListener {
	private int cur_call_state = TelephonyManager.CALL_STATE_IDLE;
	private int cur_data_state = TelephonyManager.DATA_ACTIVITY_NONE;
	private int cur_serv_state = ServiceState.STATE_POWER_OFF;
	private int cur_dsrv_state = TelephonyManager.DATA_DISCONNECTED;
	private static ScannerApplication ta;
	private static int signalStrength;

	// TODO Redirect all outputs to the database

	public PhoneStateLogger(ScannerApplication ta) {
		PhoneStateLogger.ta = ta;
	}

	// Not Ringing -> Ringing => Incoming
	// Idle -> Off-Hook => Outgoing

	// FIXME: really we should probably check the call-log
	// to get the outgoing call status.

	// TODO Redirect all outputs to the database

	public void onCallStateChanged(int state, String incomingNumber) {
		StringBuffer buf = new StringBuffer();
		if ((cur_call_state == TelephonyManager.CALL_STATE_IDLE)
				&& (state == TelephonyManager.CALL_STATE_OFFHOOK)) {
			buf.append("CALL-OUT:");
			ta.runEvent(EventTrigger.CALL_OUTGOING.name());
		} else if ((state == TelephonyManager.CALL_STATE_RINGING)
				&& (cur_call_state != TelephonyManager.CALL_STATE_RINGING)) {
			buf.append("CALL-IN:");
			ta.runEvent(EventTrigger.CALL_INCOMING.name());
			buf.append(incomingNumber);
			buf.append(",");
		}

		// update the state regardless
		cur_call_state = state;

		// drop out if there's nothing to do
		if (buf.length() <= 0)
			return;
		// TrackerService.writeLine(buf, out);
	}

	public void onSignalStrengthChanged(int asu) {
		PhoneStateLogger.signalStrength = asu;
	}

	public static int getSignalStrength() {
		return PhoneStateLogger.signalStrength;
	}

	public void onServiceStateChanged(int state) {
		StringBuffer buf = new StringBuffer();
		if ((state == ServiceState.STATE_IN_SERVICE)
				&& (cur_serv_state != ServiceState.STATE_IN_SERVICE)) {
			buf.append("SERVICE-ON:");
		} else if ((state != ServiceState.STATE_IN_SERVICE)
				&& (cur_serv_state == ServiceState.STATE_IN_SERVICE)) {
			buf.append("SERVICE-OFF:");
		}
		cur_serv_state = state;
		if (buf.length() <= 0)
			return;
		// TrackerService.writeLine(buf, out);
	}

	public void onDataConnectionStateChanged(int state) {
		StringBuffer buf = new StringBuffer();
		if ((state == TelephonyManager.DATA_CONNECTED)
				&& (cur_dsrv_state != TelephonyManager.DATA_CONNECTED)) {
			buf.append("DATA-ON:");
		} else if ((state != TelephonyManager.DATA_CONNECTED)
				&& (cur_dsrv_state == TelephonyManager.DATA_CONNECTED)) {
			buf.append("DATA-OFF:");
		}
		cur_dsrv_state = state;
		if (buf.length() <= 0)
			return;
		// TrackerService.writeLine(buf, out);
	}

	public void onDataActivity(int direction) {
		StringBuffer buf = new StringBuffer();
		if (((direction == TelephonyManager.DATA_ACTIVITY_IN)
				|| (direction == TelephonyManager.DATA_ACTIVITY_OUT) || (direction == TelephonyManager.DATA_ACTIVITY_INOUT))
				&& (cur_data_state == TelephonyManager.DATA_ACTIVITY_NONE)) {
			buf.append("DATA-START:");
		} else if ((direction == TelephonyManager.DATA_ACTIVITY_NONE)
				&& (cur_data_state != TelephonyManager.DATA_ACTIVITY_NONE)) {
			buf.append("DATA-STOP:");
		}
		cur_data_state = direction;
		if (buf.length() <= 0)
			return;
		// TrackerService.writeLine(buf, out);
	}
}
