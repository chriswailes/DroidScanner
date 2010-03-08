package com.nuvsoft.android.scanner.observers;

import com.nuvsoft.android.scanner.ScannerApplication;
import com.nuvsoft.android.scanner.db.DatabaseAssistant;
import com.nuvsoft.android.scanner.settings.EventTrigger;
import com.nuvsoft.android.scanner.settings.LogAction;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SMSObserver extends ContentObserver {
	public enum SMSContext {
		SMS_SENT, SMS_RECEIVED
	}

	private Context mContext;
	private SMSContext lastContext;

	private static final Uri SMS_URI = Uri.parse("content://sms");
	private static final Uri SMS_OUTBOX_URI = Uri.parse("content://sms/outbox");
	private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");

	public SMSObserver(Context context, Handler h) {
		super(h);
		// setup sms listeners
		mContext = context;
		mContext.getContentResolver().registerContentObserver(SMS_URI, true,
				this);
	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	@Override
	public void onChange(boolean arg0) {
		super.onChange(arg0);
		Cursor cur =mContext.getContentResolver().query(SMS_URI, null, null,
				null, null);
		if (cur != null) {
			cur.moveToNext();
			String protocol = cur.getString(cur.getColumnIndex("protocol"));
			if (protocol == null) {
				((ScannerApplication) mContext).runEvent(EventTrigger.SMS_SENT
						.name());
				lastContext = SMSContext.SMS_SENT;
			} else {
				((ScannerApplication) mContext)
						.runEvent(EventTrigger.SMS_RECEIVED.name());
				lastContext = SMSContext.SMS_RECEIVED;
			}

			// onEvent(SMSContext.SMS_SENT);
			// onEvent(SMSContext.SMS_RECEIVED);
		}
	}

	private static Uri getSMSUri(SMSContext smsContext) {
		if (smsContext == SMSContext.SMS_RECEIVED) {
			return SMS_INBOX_URI;
		} else if (smsContext == SMSContext.SMS_SENT) {
			return SMS_OUTBOX_URI;
		} else {
			return null;
		}
	}

	public void onEvent(String action, int eventid) {
		SMSContext smsContext = null;
		if (action.compareTo(LogAction.LOG_SMS_ALL.name()) == 0) {
			smsContext = lastContext;
		} else if (action.compareTo(LogAction.LOG_SMS_INCOMING.name()) == 0) {
			smsContext = SMSContext.SMS_RECEIVED;
		} else if (action.compareTo(LogAction.LOG_SMS_OUTGOING.name()) == 0) {
			smsContext = SMSContext.SMS_SENT;
		}

		if (smsContext != null) {
			int messageSize = SMSObserver.getMostRecentSMSSize(mContext,
					smsContext);
			if (messageSize > 0) {
				Log.v(SMSObserver.class.getSimpleName(), smsContext.toString()
						+ " : " + messageSize + " bytes");
				DatabaseAssistant.logSMSResult(mContext, messageSize, smsContext
						.name(), eventid);
			}
		}
	}

	/**
	 * @param context
	 * @param smsContext
	 * @return The size of the most recently sent/received sms.
	 */
	public static int getMostRecentSMSSize(Context context,
			SMSContext smsContext) {
		Uri smsUri = getSMSUri(smsContext);

		if (smsUri != null) {
			String SORT_ORDER = "date DESC";
			Cursor cursor = context.getContentResolver().query(smsUri,
					new String[] { "body" }, null, null, SORT_ORDER);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int size = cursor.getString(cursor.getColumnIndex("body"))
							.length();
					cursor.close();
					return size;
				} else {
					cursor.close();
				}
			}
		}
		return -1;
	}

	/**
	 * @param context
	 *            The applications context from which to get the
	 *            ContentResolver.
	 * @param smsContext
	 *            The sms context (either sent or received).
	 * @param unreadOnly
	 *            Select only unread sms messages.
	 * @return A cursor which holds all sms messages from the
	 *         content://sms/(inbox|oubox) content provider given the above
	 *         constraints.
	 */
	public static Cursor getSmsDetails(Context context, SMSContext smsContext,
			boolean unreadOnly) {
		Uri smsUri = getSMSUri(smsContext);

		String SMS_READ_COLUMN = "read";
		String WHERE_CONDITION = unreadOnly ? SMS_READ_COLUMN + " = 0" : null;
		String SORT_ORDER = "date DESC";

		return smsUri != null ? context.getContentResolver().query(smsUri,
				null, WHERE_CONDITION, null, SORT_ORDER) : null;
		// Cursor cursor = context.getContentResolver().query(
		// smsUri,
		// new String[] { "_id", "thread_id", "address", "person", "date",
		// "body" }, WHERE_CONDITION, null, SORT_ORDER);
	}
}