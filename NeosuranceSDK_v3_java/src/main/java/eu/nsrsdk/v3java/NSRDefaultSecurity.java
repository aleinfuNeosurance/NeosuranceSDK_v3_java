package eu.nsrsdk.v3java;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import eu.nsrsdk.utils.NSRUtils;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NSRDefaultSecurity implements NSRSecurityDelegate {

	public static String TAG = "NSRNetworkAdapter";
	private static NSRSecurityDelegate NSRnwa = null;
	private Context context = null;

	private static ExecutorService executorService = null;

	public void secureRequest(final Context ctx, final String endpoint, final JSONObject payload, final JSONObject headers, final NSRSecurityResponse completionHandler) throws Exception {
		try {
			final String url = NSRUtils.getSettings(ctx).getString("base_url") + endpoint;
			NSRLog.d("NSRDefaultSecurity - NSRNetworkAdapter: " + url);

			if(executorService == null) {
				executorService = Executors.newFixedThreadPool(1);
			}

			executorService.execute(new Runnable() {
				@Override
				public void run() {
					doInBackground(url, payload, headers, completionHandler);
				}
			});

			//AsynchRequest asynchRequest = new AsynchRequest(url, payload, headers, completionHandler);
			//asynchRequest.execute();
		} catch (Exception e) {
			NSRLog.e(e.getMessage());
			throw e;
		}
	}

	private void doInBackground(final String url, final JSONObject payload, final JSONObject headers, final NSRSecurityResponse completionHandler){

		NSRHttpRunner httpRunner = null;
		try {
			httpRunner = new NSRHttpRunner(url);
			if (payload != null)
				httpRunner.payload(payload.toString(), "application/json");

			if (headers != null) {
				Iterator<String> keys = headers.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					httpRunner.header(key, headers.getString(key));
				}
			}
			String response = httpRunner.read();
			NSRLog.d("NSRDefaultSecurity - NSRNetworkAdapter - response:" + response);
			completionHandler.completionHandler(new JSONObject(response), null);
		} catch (Exception e) {
			try {
				if (httpRunner != null) {
					NSRLog.e("MSG:" + httpRunner.getMessage());
					NSRLog.e("Error:" + e.getMessage());
				}
				completionHandler.completionHandler(null, e.toString());
			} catch (Exception ee) {
				NSRLog.e(ee.toString());
			}
		}

	}



}