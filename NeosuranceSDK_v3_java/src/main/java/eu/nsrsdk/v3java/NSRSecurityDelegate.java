package eu.nsrsdk.v3java;

import android.content.Context;

import org.json.JSONObject;

public interface NSRSecurityDelegate {
	void secureRequest(Context ctx, String endpoint, JSONObject payload, JSONObject headers, NSRSecurityResponse completionHandler) throws Exception;
}