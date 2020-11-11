package eu.nsrsdk.v3java;

import org.json.JSONObject;

public interface NSRSecurityResponse {
	void completionHandler(JSONObject response, String error) throws Exception;
}