package eu.nsrsdk.v3java;

import android.app.PendingIntent;
import android.content.Context;

import org.json.JSONObject;

public interface NSRPushDelegate {
	PendingIntent makePendingIntent(Context ctx, JSONObject push);
}