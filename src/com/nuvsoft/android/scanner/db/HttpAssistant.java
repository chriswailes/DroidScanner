package com.nuvsoft.android.scanner.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

public class HttpAssistant {

	private static final String LOG_TAG = HttpAssistant.class.getSimpleName();
	private static String url, pass;

	private static void getServerInfo(Context context) {
		url = DatabaseAssistant.getSyncURL(context);
		pass = DatabaseAssistant.getSyncPass(context);
		Log.v(LOG_TAG, String.format("%s,%s", url, pass));
	}

	public static boolean testSettings(Context context) {
		getServerInfo(context);
		try {
			if (url != null && pass != null) {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);

				MultipartEntity entity = new MultipartEntity();
				entity.addPart("pass", new StringBody(pass));
				entity.addPart("request", new StringBody("test"));
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);

				String result = "";

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));

				String line = null;
				while ((line = reader.readLine()) != null) {
					result += line + "\n";
				}

				Log.v(LOG_TAG, result);

				reader.close();

				boolean ret = result.contains("TEST_SUCCESS");
				if (ret)
					Log.v(LOG_TAG, "SUCCESSFUL TEST!");
				else
					Log.v(LOG_TAG, "FAILED TEST.");
				return ret;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.v(LOG_TAG, e.getMessage());
			return false;
		}
	}

	public static boolean post(Context context, File f) {
		getServerInfo(context);

		if (url != null && pass != null) {
			HttpClient httpclient = new DefaultHttpClient();
			// TODO: Get host url from settings file/db.
			HttpPost httppost = new HttpPost(url);

			try {
				MultipartEntity entity = new MultipartEntity();
				// TODO: Get password from settings file/db.
				entity.addPart("pass", new StringBody(pass));
				entity.addPart("request", new StringBody("upload"));
				entity.addPart("uploadedfile", new FileBody(f));
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);

				String result = "";

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));

				String line = null;
				while ((line = reader.readLine()) != null) {
					result += line + "\n";
				}

				Log.v(LOG_TAG, result);

				reader.close();

				boolean ret = result.contains(f.getName());
				if (ret)
					Log.v(LOG_TAG, "SUCCESSFUL UPLOAD!");
				else
					Log.v(LOG_TAG, "UPLOAD FAILED.");

				return ret;
			} catch (Exception e) {
				Log.v(LOG_TAG, e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	public static List<String> getSettingsFromServer(Context context) {
		getServerInfo(context);
		List<String> ret = new LinkedList<String>();
		if (url != null && pass != null) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);

				MultipartEntity entity = new MultipartEntity();
				entity.addPart("pass", new StringBody(pass));
				entity.addPart("request", new StringBody("read_settings"));
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));

				String line = null;
				while ((line = reader.readLine()) != null) {
					Log.v(LOG_TAG, line);
					ret.add(line);
				}

				reader.close();
				return ret;

				// HttpClient httpclient = new DefaultHttpClient();
				// // TODO: Get host url from settings file/db.
				// HttpPost httppost = new HttpPost(url);
				//
				// MultipartEntity entity = new MultipartEntity();
				// // TODO: Get password from settings file/db.
				// entity.addPart("pass", new StringBody(pass));
				// entity.addPart("request", new StringBody("read_settings"));
				// httppost.setEntity(entity);
				// HttpResponse response = httpclient.execute(httppost);
				//
				// BufferedReader reader = new BufferedReader(
				// new InputStreamReader(response.getEntity().getContent()));
				// return reader;
			} catch (Exception e) {
				Log.v(LOG_TAG, e.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}
}